import {icon,esc,table,empty,api,toast,downloadCsv,autoColumns} from './ui.js';

const text=(name,label,value='',extra={})=>({name,label,type:'text',value,...extra});
const num=(name,label,value='',extra={})=>({name,label,type:'number',value,...extra});
const day=(name,label,value)=>({name,label,type:'date',value});
const choice=(name,label,options,value)=>({name,label,type:'select',options,value});
const warehouse=(value='1')=>choice('id','仓库',[['1','1 号仓 · 广州'],['2','2 号仓 · 杭州'],['3','3 号仓 · 武汉'],['4','4 号仓 · 成都']],value);
const tenant=(name='tenantId',value='1')=>choice(name,'商户',[1,2,3,4,5,6,7,8].map(n=>[String(n),`商户-${n}`]),value);
const period=(from,until)=>[day('from','开始日期',from),day('until','结束日期（不含）',until)];

export const workbenches={
 orders:[
  {key:'quick',title:'订单快速搜索',description:'按订单号、备注、客户姓名或手机号中的关键字查找订单。',path:'/orders/search',fields:[text('keyword','关键字','0099999')]},
  {key:'by-no',title:'按订单号查询',description:'输入完整订单号查看订单。',path:'/orders/by-no',fields:[text('orderNo','订单号','SO2025000012345')]},
  {key:'suffix',title:'订单尾号查询',description:'客户只记得订单号末几位时使用。',path:'/orders/search/suffix',fields:[text('suffix','订单号尾号','0012345')]},
  {key:'remark',title:'配送备注检索',description:'在待处理订单的配送备注中查找关键字。',path:'/orders/search/remark',fields:[text('keyword','备注关键字','备注-12345')]},
  {key:'advanced',title:'订单高级搜索',description:'同时匹配订单、客户与收货地址，结果按金额排序。',path:'/orders/search/advanced',fields:[text('keyword','关键字','0099999')]},
  {key:'product',title:'按商品查订单',description:'查找包含指定商品的订单，按订单金额排序。',path:'/orders/by-product',fields:[text('keyword','商品名称','0')]},
  {key:'pages',title:'订单分页浏览',description:'按下单时间倒序翻阅全部订单。',path:'/orders/pages',fields:[num('page','页码','4001',{min:1}),num('size','每页条数','20',{min:1,max:100})]},
  {key:'amount-pages',title:'订单金额排行',description:'按订单金额从高到低分页浏览。',path:'/orders/pages/by-amount',fields:[num('page','页码','3001',{min:1}),num('size','每页条数','20',{min:1,max:100})]},
  {key:'item-pages',title:'订单明细分页',description:'按商品明细逐页核对成交记录。',path:'/orders/items/pages',fields:[num('page','页码','6401',{min:1}),num('size','每页条数','50',{min:1,max:100})]},
  {key:'count',title:'期间订单数',description:'统计期间内的有效订单总数。',path:'/orders/count',fields:period('2025-01-01','2026-01-01')},
  {key:'export',title:'订单导出',description:'导出订单及支付、运单、售后摘要，最长 31 天。',path:'/orders/export',fields:period('2025-06-01','2025-06-21')},
  {key:'duplicates',title:'疑似重复下单',description:'同一客户在指定小时内多次下单的记录。',path:'/orders/duplicates',fields:[...period('2025-06-01','2025-06-15'),num('hours','间隔小时','24',{min:1,max:168})]},
  {key:'anomalies',title:'订单金额异常',description:'订单金额明显高于客户历史平均客单价。',path:'/orders/anomalies',fields:[...period('2025-06-01','2025-08-01'),num('ratio','倍数','1.5',{min:1.1,max:10,step:0.1})]},
  {key:'amount-check',title:'订单金额核对',description:'核对客户订单总额与商品明细合计。',path:'/orders/amount-check',fields:[num('customerId','客户编号','1',{min:1})]},
  {key:'margins',title:'客户订单毛利',description:'按订单查看客户的收入、成本与毛利。',path:'/orders/margins',fields:[num('customerId','客户编号','1',{min:1})]},
  {key:'promotion',title:'促销码订单',description:'使用指定促销码的订单及优惠金额。',path:'/orders/promotion',fields:[text('code','促销码','PROMO-9'),...period('2025-01-01','2026-01-01')]},
  {key:'calendar',title:'订单日历',description:'按天查看下单、支付与取消数量。',path:'/orders/calendar',fields:period('2025-06-01','2025-07-21')},
  {key:'tenant-ledger',title:'商户成交流水',description:'商户订单逐笔成交及累计成交额。',path:'/orders/tenant-ledger',fields:[tenant('tenantId','2'),...period('2025-06-01','2025-06-28')]},
  {key:'channels',title:'渠道经营看板',description:'各销售渠道的订单数、成交额与件数。',path:'/dashboard/channels',fields:period('2025-01-01','2026-01-01')}
 ],
 customers:[
  {key:'search',title:'客户搜索',description:'按姓名或邮箱查找客户，按累计消费排序。',path:'/customers/search',fields:[text('keyword','姓名或邮箱','客户-1')]},
  {key:'latest',title:'来电查最近订单',description:'根据来电手机号查看客户最近一笔有效订单。',path:'/customers/by-phone/latest-order',fields:[text('phone','手机号','13800000001')]},
  {key:'overview',title:'客户概览',description:'订单、消费与售后汇总。',path:'/customers/{id}/overview',fields:[num('id','客户编号','1',{min:1})]},
  {key:'addresses',title:'客户收货地址',description:'客户名下的全部收货地址。',path:'/customers/{id}/addresses',fields:[num('id','客户编号','1',{min:1})]},
  {key:'ranking',title:'客户消费排名',description:'客户在所属商户内的累计消费排名。',path:'/customers/{id}/ranking',fields:[num('id','客户编号','1',{min:1})]},
  {key:'timeline',title:'客户动态',description:'下单、支付、发货、售后与出库记录时间线。',path:'/customers/{id}/timeline',fields:[num('id','客户编号','1',{min:1})]},
  {key:'related',title:'客户相关订单',description:'客户下单或申请过售后的订单。',path:'/customers/{id}/related-orders',fields:[num('id','客户编号','1',{min:1})]},
  {key:'fulfillment',title:'客户订单履约',description:'客户订单的商品行数与已出库件数。',path:'/customers/{id}/fulfillment',fields:[num('id','客户编号','1',{min:1})]},
  {key:'outbound',title:'客户出库记录',description:'客户订单对应的库存出库流水。',path:'/customers/{id}/outbound',fields:[num('id','客户编号','1',{min:1})]},
  {key:'frequent',title:'客户常购商品',description:'客户购买次数最多的商品。',path:'/customers/{id}/frequent-skus',fields:[num('id','客户编号','1',{min:1})]},
  {key:'segments',title:'商户客户分群',description:'商户全部客户的订单、收款、售后与优惠券情况。',path:'/customers/segments',fields:[tenant()]},
  {key:'upgrade',title:'会员升级候选',description:'年度消费达到门槛的普通及银卡会员。',path:'/customers/upgrade-candidates',fields:[num('year','年度','2025',{min:2020}),num('threshold','消费门槛','20000',{min:0})]}
 ],
 products:[
  {key:'search',title:'商品搜索',description:'按商品名称查找，按累计销量排序。',path:'/products/search',fields:[text('keyword','商品名称','0')]},
  {key:'skus',title:'商品规格',description:'商品下的 SKU 规格、售价与重量。',path:'/products/{id}/skus',fields:[num('id','商品编号','1',{min:1})]},
  {key:'offline-stock',title:'下架商品库存',description:'已下架但仍有可用库存的商品。',path:'/products/offline-stock',fields:[]},
  {key:'best-sellers',title:'品牌畅销商品',description:'品牌下累计销量最高的商品。',path:'/brands/{id}/best-sellers',fields:[num('id','品牌编号','7',{min:1})]},
  {key:'latest-prices',title:'品牌最近成交价',description:'品牌各 SKU 最近一次成交价与当前售价。',path:'/brands/{id}/latest-prices',fields:[num('id','品牌编号','7',{min:1})]},
  {key:'return-rates',title:'品类退货率',description:'品类内退货率最高的 SKU。',path:'/categories/{id}/return-rates',fields:[num('id','品类编号','5',{min:1})]},
  {key:'first-buyers',title:'品类商品首购',description:'品类内各 SKU 的首位购买客户。',path:'/categories/{id}/first-buyers',fields:[num('id','品类编号','5',{min:1})]},
  {key:'promotions',title:'促销活动',description:'促销活动及使用明细数、优惠金额。',path:'/promotions',fields:[choice('status','活动状态',[['ACTIVE','进行中'],['ENDED','已结束']],'ACTIVE')]},
  {key:'promotion-daily',title:'促销活动日销量',description:'促销码在活动期间每天的使用件数。',path:'/promotions/{code}/daily',fields:[text('code','促销码','PROMO-9'),...period('2025-04-10','2025-05-21')]},
  {key:'coupons',title:'优惠券门槛分析',description:'商户优惠券门槛可覆盖的订单数与金额。',path:'/coupons/thresholds',fields:[tenant(),...period('2025-04-01','2026-01-01')]}
 ],
 inventory:[
  {key:'daily-items',title:'仓库当日出库明细',description:'指定日期仓库订单商品明细。',path:'/warehouses/{id}/daily-items',fields:[warehouse('2'),day('day','日期','2025-06-18')]},
  {key:'stock-by-sku',title:'SKU 库存分布',description:'按 SKU 编码查看各仓库库存。',path:'/inventory/by-sku',fields:[text('code','SKU 编码','SKU-00000001')]},
  {key:'daily',title:'仓库日报',description:'按天统计仓库订单、出库与入库。',path:'/warehouses/{id}/daily',fields:[warehouse(),...period('2025-06-01','2025-06-22')]},
  {key:'ledger',title:'库存流水账',description:'仓库库存流水及逐笔结存。',path:'/warehouses/{id}/ledger',fields:[warehouse(),...period('2025-03-01','2025-03-06')]},
  {key:'reconciliation',title:'库存核对',description:'当前库存与流水净额差异最大的 SKU。',path:'/warehouses/{id}/reconciliation',fields:[warehouse()]},
  {key:'stagnant',title:'滞销商品',description:'指定日期后在本仓无成交的有库存商品。',path:'/warehouses/{id}/stagnant-skus',fields:[warehouse(),day('since','起始日期','2025-07-01')]},
  {key:'top-sellers',title:'仓库畅销商品',description:'仓库期间内销量最高的商品。',path:'/warehouses/{id}/top-sellers',fields:[warehouse(),...period('2025-05-01','2026-01-01')]},
  {key:'pending',title:'待出库订单',description:'已支付但尚无出库流水的订单。',path:'/warehouses/{id}/pending-outbound',fields:[warehouse('2'),...period('2025-06-01','2025-06-06')]},
  {key:'shipped-check',title:'发货出库核对',description:'已发货订单的下单件数与出库件数。',path:'/warehouses/{id}/shipped-check',fields:[warehouse(),...period('2025-06-01','2025-06-05')]}
 ],
 purchases:[
  {key:'by-no',title:'按采购单号查询',description:'输入采购单号查看采购单。',path:'/purchases/by-no',fields:[text('poNo','采购单号','PO-000123')]},
  {key:'supplier-receipts',title:'供应商采购入库',description:'供应商采购单的采购、收货与入库数量。',path:'/suppliers/{id}/receipts',fields:[num('id','供应商编号','11',{min:1})]},
  {key:'warehouse-receipts',title:'仓库到货核对',description:'仓库期间内采购单的采购与入库数量。',path:'/warehouses/{id}/receipts',fields:[warehouse(),...period('2025-06-01','2025-07-06')]},
  {key:'sku-sales',title:'供应商商品动销',description:'供应商商品期间内的销量、销售额与购买人数。',path:'/suppliers/{id}/sku-sales',fields:[num('id','供应商编号','11',{min:1}),...period('2025-01-01','2026-01-01')]},
  {key:'restock',title:'补货建议',description:'供应商商品近 60 天销量与当前库存。',path:'/suppliers/{id}/restock-suggestions',fields:[num('id','供应商编号','11',{min:1}),day('asOf','截止日期','2025-12-31')]}
 ],
 returns:[
  {key:'by-no',title:'按售后单号查询',description:'输入售后单号查看售后申请。',path:'/returns/by-no',fields:[text('returnNo','售后单号','RT-000123')]},
  {key:'restock-check',title:'退货回库核对',description:'售后申请的退回数量与回库数量。',path:'/returns/restock-check',fields:period('2025-06-01','2025-06-06')},
  {key:'refund-check',title:'客户退款核对',description:'商户客户的支付退款金额与售后退款金额对比。',path:'/returns/refund-check',fields:[tenant()]},
  {key:'movements',title:'退货入库流水',description:'售后单对应的退货入库流水。',path:'/returns/movements',fields:period('2025-01-01','2025-04-01')}
 ],
 logistics:[
  {key:'search',title:'物流查询',description:'按运单号或订单号关键字查找运单。',path:'/shipments/search',fields:[text('keyword','运单号或订单号','0012345')]},
  {key:'by-tracking',title:'运单号查询',description:'输入完整运单号查看物流轨迹。',path:'/shipments/by-tracking-no',fields:[text('trackingNo','运单号','TRACK-000000012345')]},
  {key:'pages',title:'运单分页浏览',description:'按发货时间倒序翻阅运单。',path:'/shipments/pages',fields:[num('page','页码','2001',{min:1}),num('size','每页条数','20',{min:1,max:100})]},
  {key:'by-customer',title:'客户运单',description:'客户本人或其收货地址相关的运单。',path:'/shipments/by-customer',fields:[num('customerId','客户编号','1',{min:1})]},
  {key:'tracking',title:'客户物流轨迹',description:'客户各运单的最新物流节点。',path:'/shipments/tracking',fields:[num('customerId','客户编号','1',{min:1})]},
  {key:'overdue',title:'配送超时',description:'下单到签收超过指定小时的运单。',path:'/shipments/overdue',fields:[num('hours','超时小时','150',{min:24})]},
  {key:'daily',title:'物流日报',description:'按天统计发货、签收与轨迹数量。',path:'/shipments/daily',fields:period('2025-06-01','2025-07-16')}
 ],
 finance:[
  {key:'daily',title:'当日支付流水',description:'指定日期的全部支付记录。',path:'/payments/daily',fields:[day('day','日期','2025-06-18')]},
  {key:'paid-orders',title:'当日支付订单',description:'订单支付、支付流水或发货发生在指定日期的订单。',path:'/payments/paid-orders',fields:[day('day','日期','2025-06-18')]},
  {key:'by-transaction',title:'交易流水号查询',description:'输入完整交易流水号查看支付记录。',path:'/payments/by-transaction-no',fields:[text('transactionNo','交易流水号','TX-000000012345')]},
  {key:'search',title:'支付查询',description:'按交易流水号、订单号或客户手机号关键字查找。',path:'/payments/search',fields:[text('keyword','流水号或订单号','9999999')]},
  {key:'pages',title:'支付流水分页',description:'按支付时间倒序翻阅支付记录。',path:'/payments/pages',fields:[num('page','页码','1001',{min:1}),num('size','每页条数','50',{min:1,max:100})]},
  {key:'order-check',title:'订单收款核对',description:'已支付订单的订单金额与实收金额差异。',path:'/payments/order-check',fields:period('2025-01-01','2026-01-01')},
  {key:'ledger',title:'收款日记账',description:'指定支付方式的逐笔收款及累计金额。',path:'/payments/ledger',fields:[choice('method','支付方式',[['ALIPAY','支付宝'],['WECHAT','微信支付'],['CARD','银行卡'],['BANK','对公转账']],'ALIPAY'),...period('2025-07-15','2025-07-16')]},
  {key:'calendar',title:'资金日历',description:'按天统计收款、退款与订单支付金额。',path:'/payments/calendar',fields:period('2025-06-01','2025-07-09')},
  {key:'settlements',title:'平台结算明细',description:'期间内平台结算记录及对应订单。',path:'/settlements',fields:period('2025-06-01','2025-06-08')},
  {key:'settlement-summary',title:'结算批次汇总',description:'按批次汇总结算金额、佣金与未匹配笔数。',path:'/settlements/summary',fields:[text('fromBatch','起始批次','ST20250101'),text('toBatch','结束批次','ST20251231')]},
  {key:'settlement-detail',title:'结算批次对账',description:'单个结算批次逐笔对应订单信息。',path:'/settlements/{batchNo}',fields:[text('batchNo','结算批次','ST20250124')]},
  {key:'settlement-diff',title:'结算金额差异',description:'结算金额与订单金额不一致的记录。',path:'/settlements/differences',fields:[text('fromBatch','起始批次','ST20250221'),text('toBatch','结束批次','ST20250222')]}
 ]
};

let controller,version=0;
export function disposeWorkbench(){version++;controller?.abort();}

export function workbenchPanel(route){
 const tools=workbenches[route];
 if(!tools)return '';
 return `<section class="panel workbench" id="workbench"><div class="panel-head"><div><h2>业务查询</h2><p class="panel-subtitle">常用核对与查询工具</p></div><span class="subtle-tag">${tools.length} 项</span></div><div class="workbench-layout"><nav class="workbench-list" aria-label="业务查询列表">${tools.map((t,i)=>`<button type="button" class="report-option ${i===0?'selected':''}" data-tool="${t.key}" aria-pressed="${i===0}"><span class="report-option-title">${esc(t.title)}</span></button>`).join('')}</nav><div class="workbench-detail" id="workbench-detail"></div></div></section>`;
}

export function mountWorkbench(root,route,signal){
 const tools=workbenches[route];if(!tools)return;
 const panel=root.querySelector('#workbench'),detail=panel.querySelector('#workbench-detail');
 let rows=[],columns=[],selected=tools[0];
 const field=f=>{
  const id=`tool-${f.name}`;
  const input=f.type==='select'?`<select id="${id}" name="${f.name}">${f.options.map(([v,l])=>`<option value="${esc(v)}" ${String(f.value)===String(v)?'selected':''}>${esc(l)}</option>`).join('')}</select>`
   :`<input id="${id}" name="${f.name}" type="${f.type}" value="${esc(f.value)}" ${f.min!=null?`min="${f.min}"`:''} ${f.max!=null?`max="${f.max}"`:''} ${f.step!=null?`step="${f.step}"`:''} required>`;
  return `<div class="field"><label for="${id}">${esc(f.label)}</label>${input}</div>`;
 };
 function draw(){
  version++;controller?.abort();rows=[];columns=[];
  detail.innerHTML=`<div class="report-details"><h3 class="workbench-title">${esc(selected.title)}</h3><p class="report-note">${esc(selected.description)}</p><form class="report-run" id="tool-form">${selected.fields.map(field).join('')}<button class="button primary" type="submit" id="tool-run">${icon('search',14)}查询</button></form></div><div class="report-result-head"><div class="result-meta" id="tool-meta"><strong>查询结果</strong><span>等待查询</span></div><button class="button small" id="tool-export" disabled>${icon('download',13)}导出 CSV</button></div><div class="report-result-body" id="tool-result">${empty('填写条件后查询','结果将显示在这里。','search')}</div>`;
  const form=detail.querySelector('#tool-form');
  form.addEventListener('submit',async event=>{
   event.preventDefault();
   const values=Object.fromEntries(new FormData(form));
   if(values.from&&values.until&&values.from>=values.until){toast('开始日期必须早于结束日期。',true);return;}
   let path=selected.path;const query=new URLSearchParams();
   for(const [k,v] of Object.entries(values)){if(path.includes(`{${k}}`))path=path.replace(`{${k}}`,encodeURIComponent(v));else query.set(k,v);}
   controller?.abort();controller=new AbortController();const mine=++version;
   const abort=()=>controller?.abort();signal.addEventListener('abort',abort,{once:true});
   const button=detail.querySelector('#tool-run'),output=detail.querySelector('#tool-result'),meta=detail.querySelector('#tool-meta'),exportButton=detail.querySelector('#tool-export');
   button.disabled=true;button.innerHTML='<span class="spinner"></span>查询中';exportButton.disabled=true;
   meta.innerHTML='<strong>查询结果</strong><span>查询中…</span>';output.innerHTML=empty('正在查询','正在读取业务数据。','clock');
   try{
    const result=await api(`${path}${query.size?`?${query}`:''}`,{signal:controller.signal});if(mine!==version||signal.aborted)return;
    rows=Array.isArray(result)?result:[result];columns=autoColumns(rows);
    output.innerHTML=rows.length?table(columns,rows,'report-data'):empty('没有符合条件的记录','可调整条件后重新查询。','search');
    meta.innerHTML=`<span class="result-dot"></span><strong>查询完成</strong><span>${rows.length} 条记录</span>`;exportButton.disabled=!rows.length;
   }catch(error){if(error.name==='AbortError'||mine!==version)return;output.innerHTML=empty('查询失败',error.message,'search');meta.innerHTML='<strong>查询结果</strong><span>查询失败，可重新查询</span>';}
   finally{signal.removeEventListener('abort',abort);if(mine===version&&button.isConnected){button.disabled=false;button.innerHTML=`${icon('search',14)}查询`;}}
  });
  detail.querySelector('#tool-export').addEventListener('click',()=>{if(rows.length)downloadCsv(`${route}-${selected.key}.csv`,columns,rows);});
 }
 panel.querySelector('.workbench-list').addEventListener('click',event=>{
  const button=event.target.closest('[data-tool]');if(!button)return;
  selected=tools.find(t=>t.key===button.dataset.tool);
  panel.querySelectorAll('[data-tool]').forEach(b=>{const on=b===button;b.classList.toggle('selected',on);b.setAttribute('aria-pressed',String(on));});
  draw();
 });
 draw();
}
