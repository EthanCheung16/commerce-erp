import {icon,esc,table,empty,heading,refreshButton,api,downloadCsv,toast,autoColumns} from './ui.js';

const categories={SALES:'销售经营',CUSTOMERS:'客户分析',PRODUCTS:'商品分析',FULFILLMENT:'仓储履约',PROCUREMENT:'采购供应',FINANCE:'财务结算'};
let requestController,generation=0;
export function disposeReports(){generation++;requestController?.abort();}

export async function renderReports(main,signal,options={}){
 const catalog=await api('/reports',{signal});if(signal.aborted)return;
 const tenants=options.tenants?.length?options.tenants:[1,2,3,4,5,6,7,8].map(id=>({id,name:`商户-${id}`}));
 const productCategories=options.categories||[];
 let selected=catalog[0],resultRows=[],resultColumns=[];
 main.innerHTML=heading('报表中心','从业务问题出发，查看订单、商品与库存的经营数据。',refreshButton,'REPORT CENTER')+`
 <div class="reports-layout"><section class="panel report-library"><div class="panel-head"><h2>业务报表</h2><span class="subtle-tag">${catalog.length} 个报表</span></div><div class="report-controls"><div class="search-box">${icon('search',14)}<input id="report-search" type="search" aria-label="搜索报表" placeholder="搜索报表名称"></div><select id="report-category" aria-label="按业务分类筛选"><option value="">全部业务分类</option>${Object.entries(categories).map(([k,v])=>`<option value="${k}">${v}</option>`).join('')}</select><div class="report-count" id="report-count"></div></div><div class="report-list" id="report-list" aria-label="报表列表"></div></section><section class="panel" id="report-workspace"></section></div>`;
 const list=main.querySelector('#report-list'),search=main.querySelector('#report-search'),category=main.querySelector('#report-category'),workspace=main.querySelector('#report-workspace');
 function drawList(){
  const needle=search.value.trim().toLowerCase();const filtered=catalog.filter(r=>(!category.value||r.category===category.value)&&(!needle||`${r.code} ${r.title}`.toLowerCase().includes(needle)));
  main.querySelector('#report-count').textContent=`显示 ${filtered.length} / ${catalog.length} 个报表`;
  list.innerHTML=filtered.length?filtered.map(r=>`<button class="report-option ${selected?.code===r.code?'selected':''}" data-report="${r.code}" aria-pressed="${selected?.code===r.code}"><span class="report-option-top"><span>${esc(categories[r.category])}</span></span><span class="report-option-title">${esc(r.title)}</span></button>`).join(''):empty('未找到报表','试试其他关键词或业务分类。','search');
 }
 function parameterFields(report){
  const fields=[];
  if(report.parameters.includes('tenant'))fields.push(`<div class="field"><label for="report-tenant">商户</label><select id="report-tenant" name="tenantId">${tenants.map(t=>`<option value="${t.id}">${esc(t.name)}</option>`).join('')}</select></div>`);
  if(report.parameters.includes('category'))fields.push(productCategories.length?`<div class="field"><label for="report-product-category">品类</label><select id="report-product-category" name="categoryId">${productCategories.map(c=>`<option value="${c.id}" ${c.id===5?'selected':''}>${esc(c.name)}</option>`).join('')}</select></div>`
   :`<div class="field"><label for="report-product-category">品类编号</label><input id="report-product-category" name="categoryId" type="number" min="1" value="5" required></div>`);
  return fields.join('');
 }
 function drawDetail(){
  generation++;requestController?.abort();resultRows=[];resultColumns=[];
  if(!selected){workspace.innerHTML=empty('选择一个业务报表','从左侧目录选择报表后开始查询。','reports');return;}
  workspace.innerHTML=`<div class="report-details"><div class="report-kicker"><span>${esc(categories[selected.category])}</span></div><h2>${esc(selected.title)}</h2><form id="report-run" class="report-run"><div class="field"><label for="report-from">开始日期</label><input id="report-from" name="from" type="date" value="2025-01-01" required></div><div class="field"><label for="report-until">结束日期（不含当天）</label><input id="report-until" name="until" type="date" value="2026-01-01" required></div>${parameterFields(selected)}<button class="button primary" type="submit" id="run-report">${icon('reports',15)}运行报表</button></form></div><div class="report-result-head"><div class="result-meta" id="result-meta"><strong>查询结果</strong><span>等待执行</span></div><button class="button small" id="export-report" disabled>${icon('download',13)}导出 CSV</button></div><div class="report-result-body" id="report-result">${empty('准备好探索业务数据','点击“运行报表”，读取数据库中的业务结果。','reports')}</div><div class="query-note">金额单位为人民币；导出内容以本次查询结果为准。</div>`;
  const form=workspace.querySelector('#report-run');
  form.addEventListener('submit',async event=>{
   event.preventDefault();const values=Object.fromEntries(new FormData(form));
   if(values.from>=values.until){toast('开始日期必须早于结束日期。',true);return;}
   requestController?.abort();requestController=new AbortController();const mine=++generation,button=workspace.querySelector('#run-report'),output=workspace.querySelector('#report-result'),meta=workspace.querySelector('#result-meta'),exportButton=workspace.querySelector('#export-report');
   const abort=()=>requestController?.abort();signal.addEventListener('abort',abort,{once:true});
   button.disabled=true;button.innerHTML='<span class="spinner"></span>正在查询';exportButton.disabled=true;
   output.innerHTML=empty('正在生成报表','正在汇总业务数据。','clock');meta.innerHTML='<strong>查询结果</strong><span>执行中…</span>';
   try{
    const rows=await api(`/reports/${selected.code}?${new URLSearchParams(values)}`,{signal:requestController.signal});if(mine!==generation||signal.aborted)return;
    resultRows=rows;resultColumns=autoColumns(rows);
    output.innerHTML=rows.length?table(resultColumns,rows,'report-data'):empty('本次查询没有结果','可尝试调整日期，或选择其他业务报表。','reports');
    meta.innerHTML=`<span class="result-dot"></span><strong>查询完成</strong><span>${rows.length} 条结果</span>`;
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
