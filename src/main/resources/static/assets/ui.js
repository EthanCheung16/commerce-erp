const paths={
 overview:'<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
 orders:'<rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4V2m6 2V2M9 9h6m-6 4h6m-6 4h4"/>',
 products:'<path d="m12 3 9 5-9 5-9-5 9-5Zm-9 5v9l9 5 9-5V8m-9 5v9M7.5 5.5l9 5"/>',
 inventory:'<path d="m3 9 9-6 9 6v12H3V9Zm4 12V11h10v10M7 15h10M7 18h10"/>',
 purchases:'<path d="M3 3h2l3 12h11l3-9H6m2 12h11"/><circle cx="9" cy="21" r="1"/><circle cx="18" cy="21" r="1"/>',
 returns:'<path d="M8 5 3 10l5 5M3 10h12a6 6 0 0 1 0 12M19 3v7"/>',
 reports:'<path d="M4 3v18h17M9 16v-5m5 5V7m5 9v-8"/>',
 search:'<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>',
 plus:'<path d="M12 5v14M5 12h14"/>', refresh:'<path d="M20 7v5h-5M4 17v-5h5M6 7a7 7 0 0 1 12-1l2 3M4 15l2 3a7 7 0 0 0 12-1"/>',
 arrow:'<path d="M4 12h16m-6-6 6 6-6 6"/>',chevron:'<path d="m9 5 7 7-7 7"/>',
 close:'<path d="m6 6 12 12M6 18 18 6"/>',download:'<path d="M12 3v12m-5-5 5 5 5-5M4 16v5h16v-5"/>',
 menu:'<path d="M4 6h16M4 12h16M4 18h16"/>',empty:'<path d="m3 8 9-5 9 5v12H3V8Zm0 0 9 5 9-5M12 13v7"/>',
 check:'<path d="m5 12 4 4L19 6"/>',clock:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
 money:'<path d="m6 4 6 7 6-7M5 11h14M5 16h14m-7-5v10"/>',
 customers:'<circle cx="9" cy="8" r="3.5"/><path d="M3 20a6 6 0 0 1 12 0M16 4.5a3.5 3.5 0 0 1 0 7M18 14a5 5 0 0 1 3 6"/>',
 logistics:'<path d="M3 6h11v10H3zM14 9h4l3 3v4h-7"/><circle cx="7" cy="18" r="2"/><circle cx="17" cy="18" r="2"/>',
 finance:'<rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18M7 15h4"/>',
 tools:'<path d="M4 6h10M4 12h16M4 18h7"/><circle cx="17" cy="6" r="2"/><circle cx="14" cy="18" r="2"/>'
};
export const icon=(name,size=18)=>`<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${paths[name]||paths.empty}</svg>`;
export const esc=value=>String(value??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
export const number=value=>Number(value??0).toLocaleString('zh-CN',{maximumFractionDigits:2});
export const money=value=>Number(value??0).toLocaleString('zh-CN',{style:'currency',currency:'CNY'});
export const date=value=>{if(!value)return '—';const d=new Date(value);return Number.isNaN(d.getTime())?esc(value):new Intl.DateTimeFormat('zh-CN',{timeZone:'Asia/Kuala_Lumpur',year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',hour12:false}).format(d).replaceAll('/','-');};
export const states={PENDING:['待付款','amber'],PAID:['待发货','blue'],SHIPPED:['已发货','purple'],COMPLETED:['已完成','green'],CANCELLED:['已取消','gray'],REFUNDING:['退款中','red'],ACTIVE:['在售','green'],OFFLINE:['已下架','gray'],ORDERED:['待收货','amber'],RECEIVED:['已入库','green'],PARTIAL:['部分收货','blue'],REQUESTED:['待审核','amber'],APPROVED:['已批准','blue'],REFUNDED:['已退款','green'],ALLOCATED:['已分配','blue'],PACKED:['已打包','purple'],RETURNED:['已退回','red'],RESTOCK:['重新入库','green'],SCRAP:['报废','gray']};
export const channels={WEB:'网页商城',APP:'移动端',MARKETPLACE:'电商平台',STORE:'线下门店'};
export const reasons={DAMAGED:'商品破损',WRONG_ITEM:'发错商品',NO_LONGER_NEEDED:'不再需要',LATE_DELIVERY:'配送延迟'};
export const badge=value=>{const [label,color]=states[value]||[value??'—','gray'];return `<span class="badge ${color}">${esc(label)}</span>`;};
export const channel=value=>`<span class="channel-tag">${esc(channels[value]||value)}</span>`;
export function table(columns,rows,className=''){
 if(!rows.length)return empty('暂无符合条件的数据','调整筛选条件后再试一次。');
 return `<div class="table-wrap ${className}"><table><thead><tr>${columns.map(c=>`<th scope="col" class="${c.align==='right'?'text-right':''}">${esc(c.label)}</th>`).join('')}</tr></thead><tbody>${rows.map(row=>`<tr>${columns.map(c=>`<td class="${c.align==='right'?'text-right':''}">${c.render?c.render(row):esc(row[c.key]??'—')}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`;
}
export const empty=(title,description,kind='empty',action='')=>`<div class="empty-state"><span class="empty-icon">${icon(kind,26)}</span><h3>${esc(title)}</h3><p>${esc(description)}</p>${action}</div>`;
export const loading=(message='正在读取业务数据…')=>`<div class="initial-loading"><span class="spinner"></span>${esc(message)}</div>`;
export const errorView=error=>empty('暂时无法加载',error.message,'empty','<button class="button" data-action="refresh">重新加载</button>');
export function heading(title,description,actions='',eyebrow='WORKSPACE'){
 return `<div class="page-heading"><div><div class="eyebrow">${eyebrow}</div><h1>${esc(title)}</h1><p class="page-description">${esc(description)}</p></div><div class="heading-actions">${actions}</div></div>`;
}
export const refreshButton=`<button class="button" data-action="refresh">${icon('refresh',14)}刷新数据</button>`;
export const newOrderButton=`<button class="button primary" data-action="new-order">${icon('plus',14)}新建订单</button>`;
export function toast(message,isError=false){const el=document.querySelector('#toast');el.textContent=message;el.className=`toast${isError?' error':''}`;el.hidden=false;clearTimeout(toast.timer);toast.timer=setTimeout(()=>{el.hidden=true;},4000);}

export async function api(path,{signal,...options}={}){
 const controller=new AbortController();let timedOut=false;
 const timeout=setTimeout(()=>{timedOut=true;controller.abort();},35000);
 const onAbort=()=>controller.abort();
 if(signal?.aborted)controller.abort();else signal?.addEventListener('abort',onAbort,{once:true});
 try{
  const response=await fetch(`/api${path}`,{...options,signal:controller.signal,headers:{Accept:'application/json',...(options.body?{'Content-Type':'application/json'}:{}),...options.headers}});
  const data=await response.json().catch(()=>null);
  if(!response.ok)throw new Error(data?.message||({404:'没有找到对应的记录。',400:'查询参数无效，请检查输入。',409:'库存不足，请调整商品数量。'}[response.status])||`请求未能完成（HTTP ${response.status}），请稍后重试。`);
  return data;
 }catch(error){if(timedOut)throw new Error('请求超过 35 秒，请稍后重试。');if(error instanceof TypeError)throw new Error('无法连接服务，请确认 JAR 程序与 MySQL 正在运行。');throw error;}
 finally{clearTimeout(timeout);signal?.removeEventListener('abort',onAbort);}
}

export function downloadCsv(filename,columns,rows){
 const quote=value=>{let s=String(value??'');if(/^[=+@\-\t\r]/.test(s))s="'"+s;return '"'+s.replaceAll('"','""')+'"';};
 const body=[columns.map(c=>quote(c.label)).join(','),...rows.map(row=>columns.map(c=>quote(row[c.key])).join(','))].join('\r\n');
 const url=URL.createObjectURL(new Blob(['\ufeff'+body],{type:'text/csv;charset=utf-8'}));
 const a=document.createElement('a');a.href=url;a.download=filename;document.body.append(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),1000);
}

export const fieldLabels={id:'编号',order_id:'订单编号',order_no:'订单号',customer_id:'客户编号',customer_name:'客户',name:'名称',email:'邮箱',phone:'手机号',tier:'会员等级',province:'省份',city:'城市',street:'详细地址',registered_at:'注册时间',status:'状态',order_status:'订单状态',channel:'销售渠道',ordered_at:'下单时间',paid_at:'支付时间',total_amount:'订单金额',order_amount:'订单金额',discount_amount:'优惠金额',shipping_fee:'运费',remark:'备注',warehouse_id:'仓库编号',warehouse_name:'仓库',tenant_id:'商户编号',tenant_name:'商户',tenant_rank:'商户内排名',order_count:'订单数',line_count:'商品行数',return_count:'售后次数',item_amount:'明细合计',difference:'差额',first_order_id:'先下单编号',first_order_no:'先下单订单号',first_ordered_at:'先下单时间',second_order_id:'后下单编号',second_order_no:'后下单订单号',second_ordered_at:'后下单时间',units:'件数',quantity:'数量',sku_id:'SKU 编号',sku_code:'SKU 编码',code:'编码',product_id:'商品编号',product_name:'商品名称',unit_price:'成交单价',line_amount:'行金额',price:'售价',list_price:'当前售价',last_price:'最近成交价',day:'日期',sale_date:'销售日期',month:'月份',paid_count:'支付订单数',cancelled_count:'取消订单数',revenue:'销售额',cost:'成本',margin:'毛利',customer_average:'客户平均客单价',cumulative_amount:'累计成交额',total_spent:'累计消费',annual_spent:'年度消费',last_order_at:'最近下单',refund_amount:'退款金额',event_type:'事件类型',reference_no:'业务单号',occurred_at:'发生时间',amount:'金额',paid_amount:'已收金额',coupon_count:'可用优惠券',movement_id:'流水编号',shipped_units:'已出库件数',ordered_units:'下单件数',purchase_times:'购买次数',last_bought_at:'最近购买',category_name:'品类',brand_name:'品牌',sku_count:'SKU 数',sold_units:'累计销量',available_qty:'可用库存',reserved_qty:'预留库存',reorder_point:'补货阈值',last_sold_at:'最近成交',sold_lines:'成交行数',returned_lines:'退货行数',return_rate:'退货率',first_bought_at:'首购时间',discount_rate:'折扣率',starts_at:'开始时间',ends_at:'结束时间',minimum_amount:'使用门槛',valid_until:'有效期至',eligible_orders:'满足门槛订单数',eligible_amount:'满足门槛金额',item_id:'明细编号',fulfillment_status:'履约状态',created_at:'创建时间',movement_net:'流水净额',gap:'差异',outbound_units:'出库件数',inbound_units:'入库件数',movement_type:'流水类型',running_balance:'结存',po_no:'采购单号',expected_at:'预计到货',ordered_qty:'采购数量',received_qty:'已收数量',stocked_qty:'入库数量',supplier_id:'供应商编号',supplier_name:'供应商',buyers:'购买人数',sold_60d:'近 60 天销量',return_no:'售后单号',return_id:'售后编号',return_qty:'退回数量',restocked_qty:'回库数量',requested_at:'申请时间',reason:'申请原因',refunded_payment:'已退款金额',tracking_no:'运单号',carrier:'承运商',shipped_at:'发货时间',delivered_at:'签收时间',delivery_hours:'配送时长（小时）',total_hours:'全程时长（小时）',event_count:'轨迹数',shipment_id:'运单编号',location:'位置',shipped_count:'发货数',delivered_count:'签收数',transaction_no:'交易流水号',method:'支付方式',running_amount:'累计收款',received_amount:'收款金额',refunded_amount:'退款金额',order_paid_amount:'订单支付金额',batch_no:'结算批次',platform_order_no:'平台单号',settled_amount:'结算金额',commission:'平台佣金',settled_at:'结算时间',settlement_count:'结算笔数',unmatched_count:'未匹配笔数',quarter_name:'季度',metric:'指标',value:'指标值',order_value:'订单总额',refunded_count:'已退款售后',payment_count:'到账笔数',average_price:'平均成交价',packed_units:'已打包件数',outbound_weight:'出库重量（克）',cart_mentions:'加购次数',price_band:'价格带',weight_band:'重量段',tracking_events:'物流轨迹数',five_star_mentions:'五星评价数',stocked_locations:'可售库存点',purchase_batches:'采购批次',coupon_reach:'优惠券触达',first_orders:'首单数',first_order_amount:'首单金额',average_amount:'平均金额',item_discount:'明细优惠',gross_amount:'原价金额',order_sequence:'订单序次',category_rank:'品类内排名',shipment_count:'发运次数',address_mentions:'地址数',weight_grams:'重量（克）',category_id:'品类编号',freight_cost:'运费成本'};
export const valueLabels={WEB:'网页商城',APP:'移动端',MARKETPLACE:'电商平台',STORE:'线下门店',BRONZE:'普通会员',SILVER:'银卡会员',GOLD:'金卡会员',PLATINUM:'白金会员',ALIPAY:'支付宝',WECHAT:'微信支付',CARD:'银行卡',BANK:'对公转账',SF:'顺丰',YTO:'圆通',JD:'京东物流',ZTO:'中通',DELIVERED:'已签收',IN_TRANSIT:'运输中',PICKED_UP:'已揽收',SUCCESS:'支付成功',PURCHASE:'采购入库',SALE:'销售出库',RETURN:'退货入库',ADJUSTMENT:'库存调整',ORDER:'下单',PAYMENT:'支付',SHIPMENT:'发货',STOCK_OUT:'出库',ENDED:'已结束',DAMAGED:'商品破损',WRONG_ITEM:'发错商品',NO_LONGER_NEEDED:'不再需要',LATE_DELIVERY:'配送延迟'};
const moneyKeys=/^(total_amount|order_amount|discount_amount|shipping_fee|item_amount|difference|unit_price|line_amount|price|list_price|last_price|revenue|cost|margin|customer_average|cumulative_amount|total_spent|annual_spent|refund_amount|amount|paid_amount|minimum_amount|eligible_amount|refunded_payment|running_amount|received_amount|refunded_amount|order_paid_amount|settled_amount|commission|order_value|average_price|first_order_amount|average_amount|item_discount|gross_amount|freight_cost)$/;
export function formatCell(key,value){
 if(value==null)return '<span class="muted">—</span>';
 if(key==='return_rate'||key==='discount_rate')return `${(Number(value)*100).toFixed(2)}%`;
 if(moneyKeys.test(key))return money(value);
 if(/(_at|_time)$/.test(key))return date(value);
 if(key==='status'||key==='order_status'||key==='fulfillment_status')return states[value]?badge(value):esc(valueLabels[value]||value);
 if(typeof value==='number')return number(value);
 return esc(valueLabels[value]||states[value]?.[0]||value);
}
export function autoColumns(rows){
 const keys=[...new Set(rows.flatMap(r=>Object.keys(r)))];
 return keys.map(key=>({key,label:fieldLabels[key]||key,align:rows.some(r=>typeof r[key]==='number')?'right':undefined,render:r=>formatCell(key,r[key])}));
}
