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
 money:'<path d="m6 4 6 7 6-7M5 11h14M5 16h14m-7-5v10"/>'
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
