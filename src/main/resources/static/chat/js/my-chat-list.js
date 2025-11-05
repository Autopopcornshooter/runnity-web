let stompClient = null;
let currentRoomId = null;
let chatRooms = [];
let userId;

document.addEventListener("DOMContentLoaded", () => {
  // chatRooms와 userId를 Thymeleaf로 JS에 넣기
  chatRooms = chatRoomsData;
  userId = currentUserId;

  initTabs();
  filterRooms("GROUP");
  setActiveTab("GROUP");

  const m = location.pathname.match(/\/chat-room\/my-chat-list\/(\d+)$/);
  if (m && m[1]) {
    openChat(Number(m[1]));
  }
});

async function openChat(roomId) {
  currentRoomId = Number(roomId);
  document.getElementById("chatMessages").innerHTML = "";

  // 선택한 방 정보 가져오기
  const roomData = chatRooms.find(r => r.chatRoomId === Number(roomId));

  // 채팅 제목 업데이트
  document.getElementById("chatTitle").textContent = roomData.chatRoomName;

  // 채팅방 선택 시 "나가기" 버튼 보이기
  document.getElementById("exitBtn").style.display = "inline-block";
  document.getElementById("chat-input").style.display = "flex";

  const editBtn = document.getElementById("editBtn");
  if (roomData.ownerId === currentUserId) {
    editBtn.style.display = "inline-block";
    editBtn.onclick = () => {
      window.location.href = `/chat-room/edit/${Number(roomId)}`;
    };
  } else {
    editBtn.style.display = "none";
    editBtn.onclick = null;
  }

  try {
    const res = await fetch(`/api/chat-rooms/${roomId}/messages?page=0&size=30`);
    if (res.ok) {
      const page = await res.json();
      const list = page.content.reverse(); // 역순으로 정렬
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
  messageDiv.classList.add("message");
  messageDiv.classList.add(isMine ? "mine" : "other");

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
  // csrf 토큰
  const token  = document.querySelector('meta[name="_csrf"]').content;
  const header = document.querySelector('meta[name="_csrf_header"]').content;

  // 알림창 띄우기
  const confirmExit = confirm("채팅방에서 나가시겠습니까?");
  if (!confirmExit) return;

  if (stompClient) {
    stompClient.disconnect();
    stompClient = null;
  }
  let url = `/api/chats/${currentRoomId}/leave`;

  let method = "DELETE";
  const res = await fetch(url, {
    method,
    headers: {[header]: token}
  });

  if(res.ok) {
    window.location.href = "/chat-room/my-chat-list";
  }

  currentRoomId = null;

  // 채팅방 나가면 메시지 영역 초기화
  const chatMessages = document.getElementById("chatMessages");
  chatMessages.innerHTML = '<p class="placeholder">왼쪽에서 채팅방을 선택하세요.</p>';

  // 채팅 제목 초기화
  document.getElementById("chatTitle").textContent = "채팅방 선택";
});

function initTabs() {
  const groupTabBtn = document.getElementById("groupTab");
  const duoTabBtn   = document.getElementById("duoTab");

  groupTabBtn?.addEventListener("click", () => {
    setActiveTab("GROUP");
    filterRooms("GROUP");
  });

  duoTabBtn?.addEventListener("click", () => {
    setActiveTab("DUO");
    filterRooms("DUO");
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
    const isDuo = type === "DIRECT" || type === "RANDOM";
    let visible = false;

    if (mode === "GROUP") visible = isGroup;
    else if (mode === "DUO") visible = isDuo;

    li.style.display = visible ? "" : "none";
  });
}

function setActiveTab(tab) {
  const groupTabBtn = document.getElementById("groupTab");
  const duoTabBtn   = document.getElementById("duoTab");
  groupTabBtn?.classList.toggle("active", tab === "GROUP");
  duoTabBtn?.classList.toggle("active", tab === "DUO");
}
