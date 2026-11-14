import {icon,esc,number,money,date,table,empty,heading,refreshButton,api,downloadCsv,toast} from './ui.js';

const categories={SALES:'销售经营',CUSTOMERS:'客户分析',PRODUCTS:'商品分析',FULFILLMENT:'仓储履约',PROCUREMENT:'采购供应',FINANCE:'财务结算'};
const labels={dimension:'统计维度',revenue:'销售金额',units:'销售件数',line_count:'明细数量',day:'日期',month:'月份',channel:'渠道',weekday:'星期',warehouse_id:'仓库编号',category_id:'品类编号',brand_id:'品牌编号',supplier_id:'供应商编号',sku_id:'SKU 编号',customer_id:'客户编号',order_id:'订单编号',order_no:'订单号',entity_id:'对象编号',name:'商品名称',code:'SKU 编码',domain:'邮箱域名',province:'省份',prefix:'手机前缀',tier:'客户等级',status:'订单状态',promotion:'促销码',sale_date:'销售日期',age_months:'账龄（月）',discount_band:'优惠档位',price_band:'价格区间',discount_total:'优惠总额',margin:'毛利',weight_grams:'重量（克）',mean_price:'平均单价',average_price:'平均售价',sku_count:'SKU 数量',order_count:'订单数量',discount_rate:'优惠率',item_lines:'商品行数',distinct_sku_touches:'订单 SKU 合计',payment_records:'支付记录数',refunded_payment_records:'退款支付记录',shipment_count:'发货次数',delivered_shipments:'已送达次数',return_count:'售后次数',refunded_returns:'已退款售后数',returned_lines:'退货行数',review_count:'评价数量',coupon_touches:'优惠券触达',available_locations:'可用库存点',purchase_touches:'采购触达',positive_review_touches:'五星评价触达',cart_touches:'购物车触达',address_touches:'地址触达',sku_touches:'SKU 触达',event_touches:'物流事件触达',order_value:'订单总额',orders:'订单数量',discounts:'优惠金额',buyers:'购买人数',brand_count:'品牌数量',metric:'指标',value:'指标值',fulfillment_status:'履约状态'};
const dimensionLabels={WEB:'网页商城',APP:'移动端',MARKETPLACE:'电商平台',STORE:'线下门店',PENDING:'待付款',PAID:'待发货',SHIPPED:'已发货',COMPLETED:'已完成',CANCELLED:'已取消',REFUNDING:'退款中',BRONZE:'普通会员',SILVER:'银卡会员',GOLD:'金卡会员',PLATINUM:'白金会员',ALLOCATED:'已分配',PACKED:'已打包',RETURNED:'已退回',revenue:'收入',cost:'成本',margin:'毛利',units:'件数',discount:'优惠',paid:'已支付',shipped:'已发货',completed:'已完成',allocated:'已分配',packed:'已打包',web:'网页',app:'移动端',store:'门店',low:'低价格带',middle:'中价格带',high:'高价格带',q1:'第一季度',q2:'第二季度',q3:'第三季度',q4:'第四季度',discounted:'优惠商品',regular:'原价商品',total:'合计',pending:'待付款',cancelled:'已取消',refunding:'退款中',single:'单件',double:'两件',bulk:'多件',promoted:'促销',organic:'非促销',free_shipping:'包邮',paid_shipping:'付费配送',order_discount:'订单优惠',light:'轻件',medium:'中等重量',heavy:'重件',weekday:'工作日',weekend:'周末',all_days:'全部日期'};
let requestController,generation=0;
export function disposeReports(){generation++;requestController?.abort();}

export async function renderReports(main,signal){
 const catalog=await api('/reports',{signal});if(signal.aborted)return;
 let selected=catalog[0],resultRows=[],resultColumns=[];
 main.innerHTML=heading('报表中心','从业务问题出发，查看订单、商品与库存的经营数据。',refreshButton,'REPORT CENTER')+`
 <div class="reports-layout"><section class="panel report-library"><div class="panel-head"><h2>业务报表</h2><span class="subtle-tag">${catalog.length} 个报表</span></div><div class="report-controls"><div class="search-box">${icon('search',14)}<input id="report-search" type="search" aria-label="搜索报表" placeholder="搜索报表名称"></div><select id="report-category" aria-label="按业务分类筛选"><option value="">全部业务分类</option>${Object.entries(categories).map(([k,v])=>`<option value="${k}">${v}</option>`).join('')}</select><div class="report-count" id="report-count"></div></div><div class="report-list" id="report-list" aria-label="报表列表"></div></section><section class="panel" id="report-workspace"></section></div>`;
 const list=main.querySelector('#report-list'),search=main.querySelector('#report-search'),category=main.querySelector('#report-category'),workspace=main.querySelector('#report-workspace');
 function drawList(){
  const needle=search.value.trim().toLowerCase();const filtered=catalog.filter(r=>(!category.value||r.category===category.value)&&(!needle||`${r.code} ${r.title}`.toLowerCase().includes(needle)));
  main.querySelector('#report-count').textContent=`显示 ${filtered.length} / ${catalog.length} 个报表`;
  list.innerHTML=filtered.length?filtered.map(r=>`<button class="report-option ${selected?.code===r.code?'selected':''}" data-report="${r.code}" aria-pressed="${selected?.code===r.code}"><span class="report-option-top"><span>${esc(categories[r.category])}</span></span><span class="report-option-title">${esc(r.title)}</span></button>`).join(''):empty('未找到报表','试试其他关键词或业务分类。','search');
 }
 function drawDetail(){
  generation++;requestController?.abort();resultRows=[];resultColumns=[];
  if(!selected){workspace.innerHTML=empty('选择一个业务报表','从左侧目录选择报表后开始查询。','reports');return;}
  workspace.innerHTML=`<div class="report-details"><div class="report-kicker"><span>${esc(categories[selected.category])}</span></div><h2>${esc(selected.title)}</h2><p class="report-note">按报表业务口径查询，最多展示 50 条结果。</p><form id="report-run" class="report-run"><div class="field"><label for="report-from">开始日期</label><input id="report-from" name="from" type="date" value="2025-01-01" required></div><div class="field"><label for="report-until">结束日期（不含当天）</label><input id="report-until" name="until" type="date" value="2026-01-01" required></div><button class="button primary" type="submit" id="run-report">${icon('reports',15)}运行报表</button></form><p class="report-note">日期范围适用于期间统计；累计与状态类报表按对应业务口径汇总。</p></div><div class="report-result-head"><div class="result-meta" id="result-meta"><strong>查询结果</strong><span>等待执行</span></div><button class="button small" id="export-report" disabled>${icon('download',13)}导出 CSV</button></div><div class="report-result-body" id="report-result">${empty('准备好探索业务数据','点击“运行报表”，实时读取数据库中的业务结果。','reports')}</div><div class="query-note">金额单位为人民币；导出内容以本次查询结果为准。</div>`;
  const form=workspace.querySelector('#report-run');
  form.addEventListener('submit',async event=>{
   event.preventDefault();const from=form.elements.from.value,until=form.elements.until.value;
   if(from>=until){toast('开始日期必须早于结束日期。',true);return;}
   requestController?.abort();requestController=new AbortController();const mine=++generation,button=workspace.querySelector('#run-report'),output=workspace.querySelector('#report-result'),meta=workspace.querySelector('#result-meta'),exportButton=workspace.querySelector('#export-report');
   const abort=()=>requestController?.abort();signal.addEventListener('abort',abort,{once:true});
   button.disabled=true;button.innerHTML='<span class="spinner"></span>正在查询';exportButton.disabled=true;
   output.innerHTML=empty('正在生成报表','查询可能需要几秒钟，请稍候。','clock');meta.innerHTML='<strong>查询结果</strong><span>执行中…</span>';
   const started=performance.now();
   try{
    const rows=await api(`/reports/${selected.code}?${new URLSearchParams({from,until})}`,{signal:requestController.signal});if(mine!==generation||signal.aborted)return;
    const elapsed=performance.now()-started;
    resultRows=rows;const keys=[...new Set(rows.flatMap(r=>Object.keys(r)))];const priority=['dimension','entity_id','order_id','order_no','day','month','sale_date','code','name','metric'];
    keys.sort((a,b)=>(priority.includes(a)?priority.indexOf(a):100)-(priority.includes(b)?priority.indexOf(b):100));
    resultColumns=keys.map(key=>({key,label:labels[key]||key,align:typeof rows[0]?.[key]==='number'?'right':undefined,render:r=>formatValue(key,r[key])}));
    output.innerHTML=rows.length?table(resultColumns,rows,'report-data'):empty('本次查询没有结果','可尝试调整日期，或选择其他业务报表。','reports');
    meta.innerHTML=`<span class="result-dot"></span><strong>查询完成</strong><span>${rows.length} 条结果</span><span title="包含网络与结果处理">接口响应 ${(elapsed/1000).toFixed(2)} 秒</span>`;
    exportButton.disabled=!rows.length;
   }catch(error){if(error.name==='AbortError'||mine!==generation)return;output.innerHTML=empty('报表查询失败',error.message,'reports');meta.innerHTML='<strong>查询结果</strong><span>执行失败，可重新运行</span>';}
   finally{signal.removeEventListener('abort',abort);if(mine===generation&&button.isConnected){button.disabled=false;button.innerHTML=`${icon('reports',15)}运行报表`;}}
  });
  workspace.querySelector('#export-report').addEventListener('click',()=>{if(resultRows.length)downloadCsv(`${selected.code}-${selected.title}.csv`,resultColumns,resultRows);});
 }
 search.addEventListener('input',drawList);category.addEventListener('change',drawList);
 list.addEventListener('click',event=>{const button=event.target.closest('[data-report]');if(!button)return;selected=catalog.find(r=>r.code===button.dataset.report);drawList();drawDetail();});
 drawList();drawDetail();
}

function formatValue(key,value){
 if(value==null)return '<span class="muted">—</span>';
 if(key==='discount_rate')return `${(Number(value)*100).toFixed(2)}%`;
 if(/^(revenue|margin|discount_total|discounts|mean_price|average_price|order_value|line_amount|total_amount)$/.test(key))return money(value);
 if(typeof value==='number')return number(value);
 if(/_at$/.test(key))return date(value);
 return esc(dimensionLabels[value]||value);
}
