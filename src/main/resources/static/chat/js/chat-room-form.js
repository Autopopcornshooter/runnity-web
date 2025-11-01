// 현재 페이지가 생성모드인지 수정모드인지 판별
// 예시: ?mode=edit&id=3 형태로 들어온다고 가정
document.addEventListener("DOMContentLoaded", async () => {
  const pathParts = window.location.pathname.split("/");
  const chatRoomId = pathParts[pathParts.length - 1]; // 마지막 부분이 roomId
  const mode = pathParts.includes("edit") ? "edit" : "create";

  console.log(mode);
  console.log(chatRoomId);

  // if (mode === "edit" && chatRoomId) {
  //   document.getElementById("chatRoomId").value = chatRoomId;
  //
  //   // 기존 방 데이터 불러오기
  //   const res = await fetch(`/api/chats/${chatRoomId}`);
  //   if (res.ok) {
  //     const data = await res.json();
  //     document.getElementById("chatRoomName").value = data.chatRoomName;
  //     document.getElementById("chatRoomDesc").value = data.description || "";
  //     document.getElementById("chatRoomRegion").value = data.region || "";
  //     if (data.imageUrl) document.getElementById("preview").src = data.imageUrl;
  //   }
  // }

  // 이미지 미리보기
  const imageInput = document.getElementById('imageInput');
  const preview = document.getElementById('preview');

  imageInput.addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (!file) return;

    if (file) {
      const reader = new FileReader();
      reader.onload = function(e) {
        preview.src = e.target.result;
      }
      reader.readAsDataURL(file);
    }
  });

  // 채팅방 폼 제출
  document.getElementById("chatRoomForm").addEventListener("submit", async event => {
    event.preventDefault();

    const payload = {
      chatRoomName: document.getElementById("chatRoomName").value,
      description: document.getElementById("chatRoomDesc").value,
      region: document.getElementById("chatRoomRegion").value,
      chatRoomType: "GROUP",
      imageUrl: "/images/test-image.png"
    };

    let url = "/api/chats";
    let method = "POST";

    if (mode === "edit" && chatRoomId) {
      url = `/api/chats/${chatRoomId}`;
      method = "PUT";
    }

    const res = await fetch(url, {
      method,
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      alert(mode === "create" ? "채팅방이 생성되었습니다!" : "채팅방이 수정되었습니다!");
      window.location.href = "/chat-room/list";
    } else {
      alert("오류가 발생했습니다. 다시 시도해주세요.");
    }
  });
});
