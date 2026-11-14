import {icon,esc,number,money,date,badge,channel,channels,reasons,states,table,empty,loading,errorView,heading,refreshButton,newOrderButton,toast,api} from './ui.js';
import {renderReports,disposeReports} from './reports.js';
import {openDetail,openNewOrder,configureDialogs} from './dialogs.js';

const routes={overview:['业务总览','overview'],orders:['订单管理','orders'],products:['商品目录','products'],inventory:['库存管理','inventory'],purchases:['采购管理','purchases'],returns:['售后管理','returns'],reports:['报表中心','reports']};
const main=document.querySelector('#main');
let route='overview',requestController,viewNumber=0,options={categories:[],warehouses:[]};
const listState={};
const statusOptions=keys=>'<option value="">全部状态</option>'+keys.map(k=>`<option value="${k}">${states[k][0]}</option>`).join('');
const fields={
 orders:()=>`<div class="field"><label for="customer-filter">客户编号</label><input id="customer-filter" name="customerId" type="number" min="1" placeholder="输入客户 ID"></div><div class="field"><label for="status-filter">订单状态</label><select id="status-filter" name="status">${statusOptions(['PENDING','PAID','SHIPPED','COMPLETED','CANCELLED','REFUNDING'])}</select></div>`,
 products:()=>`<div class="field"><label for="category-filter">商品品类</label><select id="category-filter" name="categoryId"><option value="">全部品类</option>${options.categories.map(c=>`<option value="${c.id}">${esc(c.name)}</option>`).join('')}</select></div>`,
 inventory:()=>`<div class="field"><label for="warehouse-filter">选择仓库</label><select id="warehouse-filter" name="warehouseId">${options.warehouses.map(w=>`<option value="${w.id}">${esc(w.name)} · ${esc(w.city)}</option>`).join('')}</select></div>`,
 purchases:()=>`<div class="field"><label for="status-filter">采购状态</label><select id="status-filter" name="status">${statusOptions(['ORDERED','PARTIAL','RECEIVED'])}</select></div>`,
 returns:()=>`<div class="field"><label for="status-filter">售后状态</label><select id="status-filter" name="status">${statusOptions(['REQUESTED','APPROVED','REFUNDED'])}</select></div>`
};
const detailButton=(type,id,label='查看详情')=>`<button class="button ghost small" data-detail="${type}" data-id="${id}" aria-label="${esc(label)} ${id}">${esc(label)} ${icon('chevron',12)}</button>`;
const orderColumns=[
 {label:'订单编号',render:r=>`<button class="row-link" data-detail="orders" data-id="${r.id}">${esc(r.order_no)}</button>`},
 {label:'客户',render:r=>`客户 ${r.customer_id}`},{label:'销售渠道',render:r=>channel(r.channel)},
 {label:'订单金额',align:'right',render:r=>`<strong>${money(r.total_amount)}</strong>`},
 {label:'状态',render:r=>badge(r.status)},{label:'下单时间',render:r=>date(r.ordered_at)},
 {label:'操作',align:'right',render:r=>detailButton('orders',r.id)}
];
const columns={
 orders:orderColumns,
 products:[{label:'商品编号',render:r=>`<span class="muted">#${r.id}</span>`},{label:'商品名称',render:r=>`<strong>${esc(r.name)}</strong>`},{key:'category_name',label:'品类'},{key:'brand_name',label:'品牌'},{label:'状态',render:r=>badge(r.status)},{label:'操作',align:'right',render:r=>detailButton('products',r.id,'查看 SKU')}],
 inventory:[{label:'SKU 编码',render:r=>`<strong>${esc(r.code)}</strong><span class="cell-sub">SKU ID · ${r.sku_id}</span>`},{label:'售价',align:'right',render:r=>money(r.price)},{label:'可用库存',align:'right',render:r=>`<strong>${number(r.available_qty)}</strong>`},{label:'已预留',align:'right',render:r=>number(r.reserved_qty)},{label:'补货阈值',align:'right',render:r=>number(r.reorder_point)},{label:'库存状态',render:r=>`<span class="badge ${r.available_qty<=r.reorder_point?'amber':''}">${r.available_qty<=r.reorder_point?'待补货':'库存充足'}</span>`}],
 purchases:[{label:'采购单号',render:r=>`<button class="row-link" data-detail="purchases" data-id="${r.id}">PO-${String(r.id).padStart(6,'0')}</button>`},{key:'supplier_name',label:'供应商'},{key:'warehouse_name',label:'收货仓库'},{label:'状态',render:r=>badge(r.status)},{label:'采购时间',render:r=>date(r.ordered_at)},{label:'预计到货',render:r=>date(r.expected_at)},{label:'操作',align:'right',render:r=>detailButton('purchases',r.id)}],
 returns:[{label:'售后单号',render:r=>`<button class="row-link" data-detail="returns" data-id="${r.id}">RT-${String(r.id).padStart(6,'0')}</button>`},{key:'customer_name',label:'客户'},{label:'关联订单',render:r=>`<button class="row-link" data-detail="orders" data-id="${r.order_id}">#${r.order_id}</button>`},{label:'申请原因',render:r=>esc(reasons[r.reason]||r.reason)},{label:'退款金额',align:'right',render:r=>money(r.refund_amount)},{label:'状态',render:r=>badge(r.status)},{label:'操作',align:'right',render:r=>detailButton('returns',r.id)}]
};
const descriptions={orders:'查看订单流转，管理交易与商品明细。',products:'按品类浏览商品，查看 SKU 规格与售价。',inventory:'掌握各仓库可用库存、预留数量与补货状态。',purchases:'跟进采购订单与供应商交付，查看收货明细。',returns:'查看售后申请、退款进度与退回商品。'};
const cursors={orders:'beforeId',products:'afterId',inventory:'afterSkuId',purchases:'beforeId',returns:'beforeId'};

function connection(ok){const el=document.querySelector('#service-state');el.className=`service-state ${ok?'connected':'disconnected'}`;el.innerHTML=`<i></i>${ok?'服务已连接':'服务连接异常'}`;}
function navigate(){const key=location.hash.slice(1).split('?')[0]||'overview';route=routes[key]?key:'overview';document.querySelectorAll('.nav-item').forEach(a=>{const active=a.dataset.route===route;a.classList.toggle('active',active);active?a.setAttribute('aria-current','page'):a.removeAttribute('aria-current');});document.querySelector('#breadcrumb-title').textContent=routes[route][0];document.title=`${routes[route][0]} · 商序 ERP`;closeNav();render();}
async function render(){
 requestController?.abort();disposeReports();requestController=new AbortController();const signal=requestController.signal,token=++viewNumber;
 main.innerHTML=loading();
 try{
  if(route==='overview')await overview(signal,token);
  else if(route==='reports')await renderReports(main,signal);
  else await renderList(signal,token);
  if(token===viewNumber)connection(true);
 }catch(error){if(error.name==='AbortError')return;if(token===viewNumber){main.innerHTML=heading(routes[route][0],'',refreshButton)+`<section class="panel">${errorView(error)}</section>`;connection(false);}}
}

async function overview(signal,token){
 const [summary,recent,stock]=await Promise.all([api('/dashboard',{signal}),api('/orders?limit=7',{signal}),api('/inventory?warehouseId=1&limit=4',{signal})]);
 if(token!==viewNumber)return;
 const o=summary.orders,iv=summary.inventory;
 const amount=Number(o.sales_amount),compact=amount>=1e8?`${(amount/1e8).toFixed(2)}<span class="metric-unit">亿元</span>`:money(amount);
 const metric=(label,value,unit,note,type)=>`<article class="metric"><div class="metric-top"><span>${label}</span><span class="metric-icon">${icon(type,16)}</span></div><div class="metric-value">${value}<span class="metric-unit">${unit}</span></div><div class="metric-bottom"><span class="mini-dot"></span>${note}</div></article>`;
 const stats=[['PENDING','pending_count','#c8b485'],['PAID','paid_count','#88ac99'],['SHIPPED','shipped_count','#92abc0'],['COMPLETED','completed_count','#3d876d'],['CANCELLED','cancelled_count','#c8d0c4'],['REFUNDING','refunding_count','#c6a195']];
 const max=Math.max(...stats.map(([,key])=>Number(o[key])),1);
 main.innerHTML=heading('业务总览','订单、库存与履约数据，尽在掌握。',refreshButton+newOrderButton,'BUSINESS OVERVIEW')+
 `<section class="metrics" aria-label="经营指标">${metric('订单总数',number(o.order_count),'笔','全部业务订单','orders')}${metric('累计成交额',compact,'','已支付、已发货及已完成订单','money')}${metric('待发货订单',number(o.paid_count),'笔','已支付，等待仓库履约','purchases')}${metric('商品规格数',number(iv.sku_count),'个',`库存预警 ${number(iv.low_stock_count)} 项`,'products')}</section>
 <div class="overview-grid"><section class="panel"><div class="panel-head"><div><h2>订单流转概览</h2><p class="panel-subtitle">当前各状态订单分布</p></div><span class="subtle-tag">全部业务数据</span></div><div class="status-chart">${stats.map(([state,key,color])=>`<div class="chart-line"><span>${states[state][0]}</span><div class="bar-track" role="img" aria-label="${states[state][0]} ${o[key]} 笔"><div class="bar-fill" style="width:${Number(o[key])/max*100}%;background:${color}"></div></div><strong>${number(o[key])}</strong></div>`).join('')}</div><div class="chart-legend"><span>共 ${number(o.order_count)} 笔订单</span><span>来源 · 实时业务数据</span></div></section>
 <section class="panel"><div class="panel-head"><div><h2>库存速览</h2><p class="panel-subtitle">1 号仓 · 商品可用库存</p></div><a class="section-link" href="#inventory">查看库存 ${icon('arrow',13)}</a></div><div class="warehouse-preview">${stock.map(r=>`<div class="stock-row"><div class="stock-title">${esc(r.code)}<span class="cell-sub">${money(r.price)} / 件</span></div><div><div class="stock-value">${number(r.available_qty)}<span class="muted"> 件可用</span></div><div class="stock-bar" title="可用库存占比 ${Math.round(r.available_qty/Math.max(1,r.available_qty+r.reserved_qty)*100)}%"><span style="width:${r.available_qty/Math.max(1,r.available_qty+r.reserved_qty)*100}%"></span></div></div></div>`).join('')}</div><div class="chart-legend"><span>库存条显示可用库存占比</span><span>已预留数量不计入可用</span></div></section></div>
 <section class="panel"><div class="panel-head"><div><h2>最新订单 <span class="subtle-tag">最近 7 笔</span></h2><p class="panel-subtitle">按创建顺序查看最近的业务记录</p></div><a class="section-link" href="#orders">全部订单 ${icon('arrow',13)}</a></div>${table(orderColumns,recent)}</section>`;
}

async function renderList(signal,token){
 const key=route;
 const state=listState[key]||(listState[key]={filters:key==='inventory'?{warehouseId:'1'}:{},stack:[null],rows:[]});
 main.innerHTML=heading(routes[key][0],descriptions[key],refreshButton+(key==='orders'?newOrderButton:''),{orders:'ORDER MANAGEMENT',products:'PRODUCT CATALOG',inventory:'WAREHOUSE INVENTORY',purchases:'PURCHASE MANAGEMENT',returns:'AFTER-SALES SERVICE'}[key])+`<section class="panel"><form id="filter-form" class="filters">${fields[key]()}<div class="filter-actions"><button class="button primary" type="submit">${icon('search',14)}查询</button><button class="button" type="reset">重置</button></div></form><div id="list-body">${loading('正在加载列表…')}</div></section>`;
 const form=document.querySelector('#filter-form');
 Object.entries(state.filters).forEach(([name,value])=>{if(form.elements.namedItem(name))form.elements.namedItem(name).value=value;});
 const runFilter=()=>{state.filters=Object.fromEntries([...new FormData(form)].filter(([,v])=>v!==''));state.stack=[null];render();};
 form.addEventListener('submit',event=>{event.preventDefault();runFilter();});
 form.addEventListener('reset',()=>setTimeout(()=>{if(form.isConnected)runFilter();},0));
 const params=new URLSearchParams({...state.filters,limit:21});
 const current=state.stack.at(-1);if(current!==null)params.set(cursors[key],current);
 const results=await api(`/${key}?${params}`,{signal});if(token!==viewNumber)return;
 state.rows=results.slice(0,20);state.more=results.length>20;
 document.querySelector('#list-body').innerHTML=table(columns[key],state.rows)+`<div class="panel-foot"><span>本页显示 ${state.rows.length} 条记录</span><div class="pager"><button class="button small" data-page="previous" ${state.stack.length===1?'disabled':''}>上一页</button><span class="page-number">${state.stack.length}</span><button class="button small" data-page="next" ${!state.more?'disabled':''}>下一页</button></div></div>`;
}

const narrowScreen=window.matchMedia('(max-width:700px)');
function closeNav(){document.querySelector('#sidebar').classList.remove('open');document.querySelector('#sidebar').inert=narrowScreen.matches;document.querySelector('#nav-scrim').hidden=true;document.querySelector('#menu-button').setAttribute('aria-expanded','false');}
document.querySelector('#navigation').innerHTML=Object.entries(routes).map(([key,[label,type]],i)=>(i===6?'<div class="nav-rule"></div>':'')+`<a href="#${key}" class="nav-item" data-route="${key}">${icon(type,17)}<span>${label}</span></a>`).join('');
document.querySelector('#menu-button').innerHTML=icon('menu',20);
document.querySelector('#menu-button').addEventListener('click',()=>{const open=!document.querySelector('#sidebar').classList.contains('open');document.querySelector('#sidebar').classList.toggle('open',open);document.querySelector('#sidebar').inert=!open&&narrowScreen.matches;document.querySelector('#nav-scrim').hidden=!open;document.querySelector('#menu-button').setAttribute('aria-expanded',String(open));});
narrowScreen.addEventListener('change',closeNav);
document.querySelector('.skip-link').addEventListener('click',event=>{event.preventDefault();main.focus();});
document.querySelector('#nav-scrim').addEventListener('click',closeNav);
document.addEventListener('keydown',e=>{if(e.key==='Escape')closeNav();});
main.addEventListener('click',async event=>{
 const target=event.target.closest('button');if(!target)return;
 if(target.dataset.action==='refresh'){render();return;}
 if(target.dataset.action==='new-order'){openNewOrder();return;}
 if(target.dataset.detail){openDetail(target.dataset.detail,Number(target.dataset.id));return;}
 if(target.dataset.page){const state=listState[route];if(target.dataset.page==='previous'&&state.stack.length>1)state.stack.pop();else if(target.dataset.page==='next'&&state.more)state.stack.push(state.rows.at(-1)[route==='inventory'?'sku_id':'id']);render();}
});
window.addEventListener('hashchange',navigate);
configureDialogs({getOptions:()=>options,onCreated:()=>{delete listState.orders;if(route==='orders'||route==='overview')render();}});
async function bootstrap(){try{options=await api('/options');connection(true);}catch(error){connection(false);toast(error.message,true);}navigate();}
bootstrap();
