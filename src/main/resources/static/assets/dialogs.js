import {icon,esc,number,money,date,badge,channel,reasons,table,empty,loading,api,toast} from './ui.js';

const dialog=document.querySelector('#detail-dialog'),content=document.querySelector('#dialog-content');
let config={getOptions:()=>({warehouses:[]}),onCreated:()=>{}},detailController,detailVersion=0;
export function configureDialogs(value){config=value;}
const closeButton=`<button type="button" class="icon-button" data-close aria-label="关闭详情">${icon('close',19)}</button>`;
function show(title,body,subtitle=''){content.innerHTML=`<div class="dialog-head"><div><h2 id="dialog-title">${esc(title)}</h2>${subtitle?`<div class="dialog-subtitle">${esc(subtitle)}</div>`:''}</div>${closeButton}</div>${body}`;if(!dialog.open)dialog.showModal();}
dialog.addEventListener('click',e=>{if(e.target.closest('[data-close]'))dialog.close();});
dialog.addEventListener('close',()=>{detailController?.abort();detailVersion++;});

export async function openDetail(type,id){
 detailController?.abort();detailController=new AbortController();const version=++detailVersion;
 const title={orders:'订单详情',purchases:'采购详情',returns:'售后详情',products:'商品规格'}[type];
 show(title,loading('正在读取详情…'));
 try{
  const result=await api(type==='products'?`/products/${id}/skus`:`/${type}/${id}`,{signal:detailController.signal});if(version!==detailVersion)return;
  if(type==='products'){
   show('商品规格',`<div class="dialog-body">${table([{label:'SKU 编码',key:'code'},{label:'SKU ID',key:'id'},{label:'规格',key:'specification'},{label:'售价',align:'right',render:r=>money(r.price)},{label:'重量',align:'right',render:r=>`${number(r.weight_grams)} 克`}],result)}</div><div class="dialog-foot"><button class="button" data-close>关闭</button></div>`,`商品 #${id}`);return;
  }
  const h=result.header;
  const field=(label,value)=>`<div class="detail-field"><span>${label}</span><strong>${value}</strong></div>`;
  const fields=type==='orders'?[['订单号',esc(h.order_no)],['订单状态',badge(h.status)],['订单金额',money(h.total_amount)],['客户',esc(h.customer_name)],['仓库',esc(h.warehouse_name)],['销售渠道',channel(h.channel)],['下单时间',date(h.ordered_at)],['订单优惠',money(h.discount_amount)],['运费',money(h.shipping_fee)]]:
    type==='purchases'?[['采购单号',esc(h.po_no)],['采购状态',badge(h.status)],['供应商',esc(h.supplier_name)],['收货仓库',esc(h.warehouse_name)],['采购时间',date(h.ordered_at)],['预计到货',date(h.expected_at)]]:
    [['售后单号',esc(h.return_no)],['处理状态',badge(h.status)],['退款金额',money(h.refund_amount)],['客户',esc(h.customer_name)],['关联订单',esc(h.order_no)],['申请原因',esc(reasons[h.reason]||h.reason)],['申请时间',date(h.requested_at)]];
  const columns=type==='orders'?[{label:'商品',render:r=>`<strong>${esc(r.product_name)}</strong><span class="cell-sub">${esc(r.sku_code)}</span>`},{label:'数量',align:'right',render:r=>number(r.quantity)},{label:'单价',align:'right',render:r=>money(r.unit_price)},{label:'小计',align:'right',render:r=>money(r.line_amount)},{label:'履约状态',render:r=>badge(r.fulfillment_status)}]:
   type==='purchases'?[{label:'SKU 编码',key:'sku_code'},{label:'采购数量',align:'right',render:r=>number(r.quantity)},{label:'已收数量',align:'right',render:r=>number(r.received_qty)},{label:'采购单价',align:'right',render:r=>money(r.unit_cost)}]:
   [{label:'SKU 编码',key:'sku_code'},{label:'原订单行',key:'order_item_id'},{label:'退回数量',align:'right',render:r=>number(r.quantity)},{label:'处理方式',render:r=>badge(r.disposition)}];
  show(title,`<div class="dialog-body"><div class="detail-grid">${fields.map(([l,v])=>field(l,v)).join('')}</div><h3 class="detail-section-title">商品明细 <span class="muted">· ${result.items.length} 项</span></h3>${table(columns,result.items)}</div><div class="dialog-foot"><button class="button" data-close>关闭</button></div>`,`记录 #${id}`);
 }catch(error){if(error.name==='AbortError'||version!==detailVersion)return;show(title,empty('详情加载失败',error.message));}
}

export function openNewOrder(){
 detailController?.abort();detailVersion++;
 const warehouses=config.getOptions().warehouses;
 show('新建订单',`<form id="new-order-form"><div class="dialog-body"><div class="form-grid"><div class="field"><label for="new-customer">客户编号</label><input id="new-customer" name="customerId" type="number" min="1" step="1" placeholder="例如：1" required></div><div class="field"><label for="new-address">收货地址编号</label><input id="new-address" name="addressId" type="number" min="1" step="1" placeholder="该客户名下的地址 ID" required></div><div class="field"><label for="new-warehouse">发货仓库</label><select id="new-warehouse" name="warehouseId" required>${warehouses.map(w=>`<option value="${w.id}">${esc(w.name)} · ${esc(w.city)}</option>`).join('')}</select></div><div class="field"><label for="new-channel">销售渠道</label><select id="new-channel" name="channel"><option value="WEB">网页商城</option><option value="APP">移动端</option><option value="MARKETPLACE">电商平台</option><option value="STORE">线下门店</option></select></div></div><h3 class="detail-section-title">商品明细</h3><div class="order-lines" id="order-lines"></div><button type="button" class="button small" id="add-line">${icon('plus',13)}添加商品</button><p class="form-hint">可在“商品目录”查看 SKU 编号。价格以当前商品售价为准，提交后自动预留库存；订单优惠和运费默认为 0。</p><div id="order-error" class="inline-error" role="alert" hidden></div></div><div class="dialog-foot"><button type="button" class="button" data-close>取消</button><button type="submit" class="button primary" id="submit-order">${icon('check',15)}创建订单</button></div></form>`,'填写客户信息与商品，创建待付款订单。');
 const form=content.querySelector('#new-order-form'),lines=content.querySelector('#order-lines'),add=content.querySelector('#add-line');let lineId=0,pending=false;
 function addLine(){
  if(lines.children.length>=20)return;
  const id=++lineId,row=document.createElement('div');row.className='line-entry';
  row.innerHTML=`<div class="field"><label for="line-sku-${id}">SKU 编号</label><input id="line-sku-${id}" data-sku type="number" min="1" step="1" placeholder="输入 SKU ID" required></div><div class="field"><label for="line-qty-${id}">数量</label><input id="line-qty-${id}" data-quantity type="number" min="1" max="100" step="1" value="1" required></div><button class="icon-button" type="button" data-remove aria-label="删除第 ${id} 行商品">${icon('close',15)}</button>`;
  lines.append(row);updateRemove();
 }
 function updateRemove(){lines.querySelectorAll('[data-remove]').forEach(b=>b.disabled=lines.children.length===1);add.disabled=lines.children.length>=20;}
 add.addEventListener('click',()=>{addLine();lines.lastElementChild.querySelector('input').focus();});
 lines.addEventListener('click',e=>{const remove=e.target.closest('[data-remove]');if(remove&&lines.children.length>1){remove.closest('.line-entry').remove();updateRemove();}});
 const preventClose=event=>{if(pending)event.preventDefault();};dialog.addEventListener('cancel',preventClose);dialog.addEventListener('close',()=>dialog.removeEventListener('cancel',preventClose),{once:true});
 form.addEventListener('submit',async event=>{
  event.preventDefault();if(pending)return;
  const errorBox=content.querySelector('#order-error'),submit=content.querySelector('#submit-order');errorBox.hidden=true;
  const items=[...lines.children].map(row=>({skuId:Number(row.querySelector('[data-sku]').value),quantity:Number(row.querySelector('[data-quantity]').value)}));
  if(new Set(items.map(i=>i.skuId)).size!==items.length){errorBox.textContent='同一 SKU 请合并为一行商品。';errorBox.hidden=false;return;}
  const body={customerId:Number(form.elements.customerId.value),addressId:Number(form.elements.addressId.value),warehouseId:Number(form.elements.warehouseId.value),channel:form.elements.channel.value,items};
  pending=true;form.querySelectorAll('button,input,select').forEach(el=>el.disabled=true);content.querySelector('[data-close]').disabled=true;submit.innerHTML='<span class="spinner"></span>正在提交';
  try{
   const result=await api('/orders',{method:'POST',body:JSON.stringify(body)});pending=false;toast(`订单 ${result.header.order_no} 已创建`);config.onCreated();openDetail('orders',Number(result.header.id));
  }catch(error){errorBox.textContent=error.message;errorBox.hidden=false;}
  finally{pending=false;if(form.isConnected){form.querySelectorAll('button,input,select').forEach(el=>el.disabled=false);content.querySelector('[data-close]').disabled=false;updateRemove();submit.innerHTML=`${icon('check',15)}创建订单`;}}
 });
 addLine();
}
