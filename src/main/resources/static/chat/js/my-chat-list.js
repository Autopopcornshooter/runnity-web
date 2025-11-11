let stomp = null;
let roomSub = null;
let notifySub = null;
let stompReadyPromise = null;

let currentRoomId = null;
let currentRoomType = null;
let chatRooms = [];
let userId;

// 공통 유틸
function $(sel, root=document){
  return root.querySelector(sel);
}

function findRoomItem(roomId){
  return $(`#chatRooms .chat-room-item[data-id="${roomId}"]`);
}

function getOrCreateBadge(li){
  let badge = li.querySelector('.unread-badge');
  if (!badge){
    badge = document.createElement('span');
    badge.className = 'unread-badge';
    (li.querySelector('.room-info') || li).appendChild(badge);
  }
  return badge;
}

function setUnread(roomId, count){
  const li = findRoomItem(roomId);
  if (!li) return;
  const badge = li.querySelector('.unread-badge') || getOrCreateBadge(li);
  if (count > 0){
    badge.textContent = count > 99 ? '99+' : String(count);
    badge.style.display = 'inline-flex';
  } else {
    badge.textContent = '';
    badge.style.display = 'none';
  }
}

function incUnread(roomId){
  const li = findRoomItem(roomId);
  if (!li) return;
  const badge = getOrCreateBadge(li);

  const cur = Number((badge.textContent || '0').replace('+','')) || 0;
  setUnread(roomId, Math.min(cur + 1, 99));
}

async function fetchJSON(url, opts={}) {
  const res = await fetch(url, { cache: 'no-store', ...opts });
  if (!res.ok) throw new Error(res.statusText);
  return res.json();
}

// 읽음 처리
async function markRoomAsRead(roomId){
  if (!roomId) return;
  try {
    const tokenMeta = $('meta[name="_csrf"]');
    const headerMeta = $('meta[name="_csrf_header"]');
    const headers = {};
    if (tokenMeta && headerMeta) headers[headerMeta.content] = tokenMeta.content;
    await fetch(`/api/chat-rooms/${roomId}/read`, { method:'PUT', headers, keepalive:true });
  } catch(e){ console.warn('markRoomAsRead failed', e); }
}

// GROUP, DUO탭
function calcTabByType(type){ return (type||'').toUpperCase()==='GROUP' ? 'GROUP' : 'DUO'; }
function updateActiveListItem(roomId){
  document.querySelectorAll("#chatRooms .chat-room-item").forEach(li=>{
    li.classList.toggle("active", Number(li.dataset.id)===Number(roomId));
  });
}
function filterRooms(mode){
  $("#chatRooms")?.querySelectorAll(".chat-room-item").forEach(li=>{
    const t = (li.dataset.type||'').toUpperCase();
    const vis = mode==='GROUP' ? t==='GROUP' : (t==='DIRECT'||t==='RANDOM');
    li.style.display = vis ? "" : "none";
  });
}
function setActiveTab(tab){
  $("#groupTab")?.classList.toggle("active", tab==='GROUP');
  $("#duoTab")?.classList.toggle("active", tab==='DUO');
}
function initTabs(){
  $("#groupTab")?.addEventListener("click", ()=>{setActiveTab("GROUP");filterRooms("GROUP");updateActiveListItem(currentRoomId);});
  $("#duoTab")?.addEventListener("click", ()=>{setActiveTab("DUO");filterRooms("DUO");updateActiveListItem(currentRoomId);});
}

// STOMP 연결
function waitFor(ms){ return new Promise(r=>setTimeout(r,ms)); }
async function waitUntilConnected(timeout=2000){
  const start = Date.now();
  while(!stomp?.connected){
    if(Date.now()-start>timeout) return false;
    await waitFor(50);
  }
  return true;
}

function connectOnce(){
  if(stompReadyPromise) return stompReadyPromise;

  stompReadyPromise = new Promise((resolve) => {
    if (stomp?.connected) { resolve(); return; }

    const socket = new SockJS("/ws-chat");
    stomp = Stomp.over(socket);
    stomp.debug = null;

    stomp.connect({}, () => {
      if (!notifySub) {
        notifySub = stomp.subscribe("/user/queue/notify", (f) => {
          const p = JSON.parse(f.body || "{}");
          if (p.type !== "NEW_MESSAGE") return;
          const rid = Number(p.roomId ?? p.chatRoomId);
          const sender = Number(p.senderId);
          if (!rid || rid === Number(currentRoomId) || sender === Number(userId)) return;
          incUnread(rid);
        });
      }
      resolve();
    });
  });

  return stompReadyPromise;
}

// 현재 방 구독
async function subscribeRoom(roomId){
  await connectOnce();
  if(roomSub) { try{ roomSub.unsubscribe(); }catch{} }
  roomSub = stomp.subscribe(`/topic/rooms.${roomId}`, (msg)=>{
    const d = JSON.parse(msg.body);
    addMessage(d.senderNickname, d.message, Number(d.senderId)===Number(userId));
  });
}

// 메시지 UI
function addMessage(sender, text, isMine){
  const c = $("#chatMessages");
  const el = document.createElement("div");
  el.className = `message ${isMine?'mine':'other'}`;
  if(!isMine){
    const s = document.createElement("span");
    s.className = "sender"; s.textContent = sender;
    el.appendChild(s);
  }
  const b = document.createElement("div");
  b.className = "bubble"; b.textContent = text;
  el.appendChild(b);
  c.appendChild(el);
  c.scrollTop = c.scrollHeight;
}

// 방 열기
async function openChat(roomId){
  if(currentRoomId && Number(currentRoomId)!==Number(roomId)) {
    await markRoomAsRead(currentRoomId);
  }

  setUnread(roomId,0);
  currentRoomId = Number(roomId);
  $("#chatMessages").innerHTML = "";

  const room = chatRooms.find(r=>r.chatRoomId===Number(roomId));
  if(!room) return;
  currentRoomType = room.chatRoomType;

  const tab = calcTabByType(currentRoomType);
  setActiveTab(tab); filterRooms(tab); updateActiveListItem(roomId);

  $("#chatTitle").textContent = room.chatRoomName;
  const desc = room.description?.trim()||'';
  const cd = $("#chatDesc");
  if(desc){ cd.textContent=desc;cd.title=desc;cd.style.display="inline-block"; }
  else { cd.textContent=''; cd.style.display='none'; }

  $("#exitBtn").style.display="inline-block";
  $("#chat-input").style.display="flex";

  const eBtn=$("#editBtn");
  if(room.ownerId===userId){ eBtn.style.display="inline-block"; eBtn.onclick=()=>location.href=`/chat-room/edit/${roomId}`;}
  else { eBtn.style.display="none"; eBtn.onclick=null; }

  const xBtn=$("#exitBtn");
  xBtn.textContent = currentRoomType==="RANDOM" ? "운동 완료" : "나가기";
  xBtn.classList.toggle("random-exit", currentRoomType==="RANDOM");

  await subscribeRoom(roomId);

  try{
    const page=await fetchJSON(`/api/chat-rooms/${roomId}/messages?page=0&size=30`);
    (page.content||[]).reverse().forEach(m=>{
      addMessage(m.senderNickname,m.message,Number(m.senderId)===Number(userId));
    });
  }catch(e){ console.error("메시지 로드 실패:", e); }

  setTimeout(()=>markRoomAsRead(currentRoomId),150);
}

// 메세지 전송
$("#sendBtn").addEventListener("click", async ()=>{
  const input=$("#messageInput");
  const msg=(input.value||'').trim();
  if(!msg||!currentRoomId) return;

  if(!(await waitUntilConnected(2000))){
    alert("서버 연결 중입니다. 잠시 후 다시 시도해주세요.");
    return;
  }

  try{
    stomp.send("/app/chat.send", {}, JSON.stringify({
      chatRoomId: currentRoomId, senderId: userId, message: msg
    }));
    input.value="";
  }catch(e){
    console.error("SEND 실패:",e);
  }
});

$("#messageInput").addEventListener("keydown", (e) => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    $("#sendBtn").click();
  }
});

// 채팅방 나가기
$("#exitBtn").addEventListener("click", async ()=>{
  const token=$('meta[name="_csrf"]').content;
  const header=$('meta[name="_csrf_header"]').content;
  const isRandom=(currentRoomType==="RANDOM");
  const msg=isRandom?"운동을 완료하시겠습니까?":"채팅방에서 나가시겠습니까?";
  if(!confirm(msg)) return;

  await markRoomAsRead(currentRoomId);
  const res=await fetch(`/api/chats/${currentRoomId}/leave`,{method:"DELETE",headers:{[header]:token}});
  if(res.ok){
    if(isRandom) alert("운동이 완료되었습니다 👟");
    location.href="/chat-room/my-chat-list";
    return;
  }
  currentRoomId=null;
  $("#chatMessages").innerHTML='<p class="placeholder">왼쪽에서 채팅방을 선택하세요.</p>';
  $("#chatTitle").textContent="채팅방 선택";
});

// 초기화
document.addEventListener("DOMContentLoaded", async ()=>{
  chatRooms=chatRoomsData;
  userId=currentUserId;
  initTabs();
  await connectOnce();

  try{
    const map=await fetchJSON('/api/chat-rooms/unread-counts');
    Object.entries(map).forEach(([rid,cnt])=>setUnread(rid,Number(cnt)));
  }
  catch{

  }

  const m=location.pathname.match(/\/chat-room\/my-chat-list\/(\d+)$/);
  if(m&&m[1]){
    const rid=Number(m[1]);
    const room=chatRooms.find(r=>r.chatRoomId===rid);
    const tab=calcTabByType(room?.chatRoomType);
    setActiveTab(tab); filterRooms(tab);
    history.replaceState(null,'','/chat-room/my-chat-list');
    await openChat(rid);
  }else{
    setActiveTab("GROUP"); filterRooms("GROUP");
  }

  const onHide=async()=>{try{await markRoomAsRead(currentRoomId);}catch{}};
  window.addEventListener('beforeunload',onHide);
  window.addEventListener('pagehide',onHide);
});