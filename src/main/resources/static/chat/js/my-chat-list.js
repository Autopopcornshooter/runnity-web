let stompClient = null;
let currentRoomId = null;
let currentRoomType = null;
let chatRooms = [];
let userId;

document.addEventListener("DOMContentLoaded", () => {
  chatRooms = chatRoomsData;
  userId = currentUserId;

  initTabs();

  // URL에 /my-chat-list/{roomId} 로 들어온 경우 먼저 탭 타입을 결정해서 세팅
  const m = location.pathname.match(/\/chat-room\/my-chat-list\/(\d+)$/);
  if (m && m[1]) {
    const preRoomId = Number(m[1]);
    const preRoom = chatRooms.find(r => r.chatRoomId === preRoomId);
    const preTab = calcTabByType(preRoom?.chatRoomType); // "GROUP" | "DUO"
    setActiveTab(preTab);
    filterRooms(preTab);
    openChat(preRoomId);
  } else {
    // 기본은 그룹 탭
    setActiveTab("GROUP");
    filterRooms("GROUP");
  }
});

// 방 타입에 따라 탭 변경 : GROUP vs DUO(DIRECT, RANDOM 포함)
function calcTabByType(type) {
  const t = (type || "").toUpperCase();
  if (t === "GROUP") return "GROUP";
  // DIRECT 또는 RANDOM(= 듀오/랜덤 1:1)
  return "DUO";
}

// 리스트의 활성 표시 관리
function updateActiveListItem(roomId) {
  const items = document.querySelectorAll("#chatRooms .chat-room-item");
  items.forEach(li => {
    const id = Number(li.getAttribute("data-id"));
    li.classList.toggle("active", id === Number(roomId));
  });
}

async function openChat(roomId) {
  currentRoomId = Number(roomId);
  document.getElementById("chatMessages").innerHTML = "";

  // 선택한 방 정보
  const roomData = chatRooms.find(r => r.chatRoomId === Number(roomId));
  if (!roomData) return;

  currentRoomType = roomData.chatRoomType;
  // 방 타입에 맞춰 탭 자동 전환 + 필터 재적용
  const tab = calcTabByType(currentRoomType);
  setActiveTab(tab);
  filterRooms(tab);

  // 리스트에서 현재 방 표시
  updateActiveListItem(roomId);

  // 채팅방 제목
  document.getElementById("chatTitle").textContent = roomData.chatRoomName;
  // 채팅방 설명
  const chatDesc = document.getElementById("chatDesc");
  const desc = roomData.description;

  if (desc && desc.trim().length > 0) {
    chatDesc.textContent = desc.trim();
    chatDesc.title = desc.trim();
    chatDesc.style.display = "inline-block";
    chatDesc.style.color = "gray";
    chatDesc.style.fontSize = "12px";
  } else {
    chatDesc.textContent = "";
    chatDesc.removeAttribute("title");
    chatDesc.style.display = "none";
  }

  // 입력/버튼 노출
  document.getElementById("exitBtn").style.display = "inline-block";
  document.getElementById("chat-input").style.display = "flex";

  const editBtn = document.getElementById("editBtn");
  if (roomData.ownerId === currentUserId) {
    editBtn.style.display = "inline-block";
    editBtn.onclick = () => window.location.href = `/chat-room/edit/${Number(roomId)}`;
  } else {
    editBtn.style.display = "none";
    editBtn.onclick = null;
  }

  const exitBtn = document.getElementById("exitBtn");
  exitBtn.classList.remove("random-exit");
  if (currentRoomType === "RANDOM") {
    exitBtn.textContent = "운동 완료";
    exitBtn.classList.add("random-exit"); // 색상 변경
  } else {
    exitBtn.textContent = "나가기";
  }

  try {
    const res = await fetch(`/api/chat-rooms/${roomId}/messages?page=0&size=30`);
    if (res.ok) {
      const page = await res.json();
      const list = page.content.reverse();
      list.forEach(m => {
        const mine = (m.senderId === userId);
        addMessage(m.senderNickname, m.message, mine);
      });
    }
  } catch (err) {
    console.error("메시지 로드 실패:", err);
  }

  connectWebSocket(roomId);
}

function addMessage(sender, text, isMine) {
  const container = document.getElementById("chatMessages");
  const messageDiv = document.createElement("div");
  messageDiv.classList.add("message", isMine ? "mine" : "other");

  const bubble = document.createElement("div");
  bubble.classList.add("bubble");
  bubble.textContent = text;

  if (!isMine) {
    const senderSpan = document.createElement("span");
    senderSpan.classList.add("sender");
    senderSpan.textContent = sender;
    messageDiv.appendChild(senderSpan);
  }

  messageDiv.appendChild(bubble);
  container.appendChild(messageDiv);
  container.scrollTop = container.scrollHeight;
}

function connectWebSocket(roomId) {
  if (stompClient) stompClient.disconnect();

  const socket = new SockJS("/ws-chat");
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    stompClient.subscribe(`/topic/rooms.${roomId}`, (msg) => {
      const data = JSON.parse(msg.body);
      const isMine = (data.senderId === userId);
      addMessage(data.senderNickname, data.message, isMine);
    });
  });
}

document.getElementById("sendBtn").addEventListener("click", () => {
  const input = document.getElementById("messageInput");
  const message = input.value.trim();
  if (message && stompClient && currentRoomId) {
    stompClient.send("/app/chat.send", {}, JSON.stringify({
      chatRoomId: currentRoomId,
      senderId: userId,
      message: message
    }));
    input.value = "";
  }
});

document.getElementById("exitBtn").addEventListener("click", async () => {
  const token  = document.querySelector('meta[name="_csrf"]').content;
  const header = document.querySelector('meta[name="_csrf_header"]').content;

  const isRandom = (currentRoomType === "RANDOM");
  const confirmMsg = isRandom ? "운동을 완료하시겠습니까?" : "채팅방에서 나가시겠습니까?";
  if (!confirm(confirmMsg)) return;

  if (stompClient) { stompClient.disconnect(); stompClient = null; }

  const res = await fetch(`/api/chats/${currentRoomId}/leave`, {
    method: "DELETE",
    headers: { [header]: token }
  });

  if (res.ok) {
    if (isRandom) {
      alert("운동이 완료되었습니다 👟");
    }
    window.location.href = "/chat-room/my-chat-list";
    return;
  }

  // 실패 시 UI 초기화만
  currentRoomId = null;
  document.getElementById("chatMessages").innerHTML =
      '<p class="placeholder">왼쪽에서 채팅방을 선택하세요.</p>';
  document.getElementById("chatTitle").textContent = "채팅방 선택";
});

function initTabs() {
  const groupTabBtn = document.getElementById("groupTab");
  const duoTabBtn   = document.getElementById("duoTab");

  groupTabBtn?.addEventListener("click", () => {
    setActiveTab("GROUP");
    filterRooms("GROUP");
    // 탭 클릭 시 현재 선택 표시도 적절히 조정
    updateActiveListItem(currentRoomId);
  });

  duoTabBtn?.addEventListener("click", () => {
    setActiveTab("DUO");
    filterRooms("DUO");
    updateActiveListItem(currentRoomId);
  });
}

// mode: "GROUP" | "DUO"
function filterRooms(mode) {
  const chatRoomsUl = document.getElementById("chatRooms");
  if (!chatRoomsUl) return;

  const items = chatRoomsUl.querySelectorAll(".chat-room-item");
  items.forEach(li => {
    const type = (li.getAttribute("data-type") || "").toUpperCase();
    const isGroup = type === "GROUP";
    const isDuo = (type === "DIRECT" || type === "RANDOM");
    let visible = (mode === "GROUP") ? isGroup : isDuo;
    li.style.display = visible ? "" : "none";
  });
}

function setActiveTab(tab) {
  const groupTabBtn = document.getElementById("groupTab");
  const duoTabBtn   = document.getElementById("duoTab");
  groupTabBtn?.classList.toggle("active", tab === "GROUP");
  duoTabBtn?.classList.toggle("active", tab === "DUO");
}