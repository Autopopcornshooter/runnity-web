const token = document.querySelector('meta[name="_csrf"]').content;
const header = document.querySelector('meta[name="_csrf_header"]').content;

// 친구 탐색/추가
document.getElementById("searchBtn").addEventListener("click", function () {
    window.open('/friend-search', 'friendSearchPopup', 'width=600,height=400,left=400,top=200');
});

// 리스트 내 친구 탐색
const searchBox = document.querySelector('.search-box');
const friendList = document.getElementById('friendList');

// ✅ 엔터 입력 시 검색 실행
searchBox.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        const keyword = searchBox.value.trim();
        if (!keyword) {               // ✔ 검색어 없으면 전체 페이지 리로드
            window.location.href = "/friendlist";
            return;
        }
        const url = keyword ? `/api/friends/searchOnList?nickname=${encodeURIComponent(keyword)}` : `/api/friends`;
        // if (!keyword) return;

        fetch(url)//`/api/friends/searchOnList?nickname=${encodeURIComponent(keyword)}`)
            .then(res => {
                if (!res.ok) throw new Error();
                return res.json();
            })
            .then(data => {
                friendList.innerHTML = ''; // 기존 목록 초기화

                if (data.length === 0) {
                    friendList.innerHTML = `<p style="text-align:center;">검색 결과가 없습니다.</p>`;
                    return;
                }

                data.forEach(friend => {
                    const div = document.createElement('div');
                    div.className = 'friend-item';
                    // 변경: data-id, data-userid 속성 추가 (핸들러에서 사용)
                    div.setAttribute('data-id', friend.id ?? '');
                    div.setAttribute('data-userid', friend.userId ?? '');
                    // 변경: likecount span 포함 (초기에는 display:none 가능)
                    const likecountText = friend.likecount != null ? `추천수 : ${friend.likecount}` : '';
                    const likecountStyle = (friend.likecount != null && friend.likecount > 0) ? 'inline-block' : 'none';
                    div.innerHTML = `
                            <span class="nickname">${friend.nickname}</span>
                            <div class="friend-button">
                                <button class="btn btn-outline-secondary btn-sm like-btn">좋아요</button>
                                <span class="likecount" th:text="${friend.likecount}" style="display:none;"></span>
                                <button class="btn btn-outline-secondary btn-sm chat-btn">채팅 시작</button>
                                <button class="btn btn-outline-secondary btn-sm delete-btn">친구 삭제</button>
                            </div>
                        `;
                    friendList.appendChild(div);
                });
            })
            .catch(() => {
                friendList.innerHTML = `<p style="text-align:center; color:red;">검색 중 오류가 발생했습니다.</p>`;
            });
    }
});


// ----------------------------
// 변경: 이벤트 위임으로 like/chat/delete 모두 처리
// ----------------------------
if (friendList) {
    friendList.addEventListener('click', function (e) {
        const likeBtn = e.target.closest('.like-btn');
        if (likeBtn) {
            const friendItem = likeBtn.closest('.friend-item');
            const friendId = friendItem ? friendItem.getAttribute('data-id') : null;

            if (!friendId) {
                console.error("❌ friendId가 없습니다. HTML data-id 속성을 확인하세요.");
                alert("유효하지 않은 사용자입니다.");
                return;
            }

            const likeCountSpan = friendItem.querySelector(".likecount");

            fetch(`/api/friends/${friendId}/like`, {
                method: "POST",
                headers: { [header]: token }
            })
                .then(response => {
                    if (!response.ok) throw new Error("서버 오류 발생");
                    return response.json();
                })
                .then(updatedCount => {
                    // 버튼 숨기고, 숫자 표시
                    likeBtn.style.display = "none";
                    if (likeCountSpan) {
                        likeCountSpan.textContent = `추천수 : ${updatedCount}`;
                        likeCountSpan.style.display = "inline-block";
                    }
                })
                .catch(error => {
                    alert("좋아요 처리 중 오류가 발생했습니다.");
                    console.error(error);
                });

            return; // 다른 핸들러 중복 실행 방지
        }

        const chatBtn = e.target.closest('.chat-btn');
        if (chatBtn) {
            const friendItem = chatBtn.closest('.friend-item');
            const friendUserId = friendItem ? friendItem.getAttribute('data-userid') : null;

            if (!friendUserId) {
                console.error("❌ friendUserId가 없습니다. HTML data-userid 속성을 확인하세요.");
                alert("유효하지 않은 사용자입니다.");
                return;
            }

            fetch("/api/friends/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [header]: token
                },
                body: JSON.stringify({ friendUserId: friendUserId })
            })
                .then(response => {
                    if (!response.ok) throw new Error("채팅방 생성 실패");
                    return response.text();
                })
                .then(roomId => {
                    // 채팅방 페이지로 이동
                    window.location.href = `/chat-room/my-chat-list`;
                })
                .catch(err => {
                    console.error(err);
                    alert("채팅방 생성 중 오류가 발생했습니다.");
                });

            return;
        }

        const deleteBtn = e.target.closest('.delete-btn');
        if (deleteBtn) {
            const friendItem = deleteBtn.closest('.friend-item');
            const friendId = friendItem ? friendItem.getAttribute('data-id') : null;

            if (!friendId) return;

            const confirmDelete = confirm("정말 이 친구를 삭제하시겠습니까?");
            if (!confirmDelete) return;

            fetch(`/api/friends/${friendId}/delete`, {
                method: "DELETE",
                headers: { [header]: token }
            })
                .then(res => {
                    if (!res.ok) throw new Error("삭제 실패");
                    return res.text();
                })
                .then(message => {
                    alert(message);
                    friendItem.remove(); // 화면에서 제거
                })
                .catch(err => {
                    console.error(err);
                    alert("친구 삭제 중 오류가 발생했습니다.");
                });

            return;
        }
    });
}
//
//
// // 좋아요 버튼
// document.addEventListener("DOMContentLoaded", () => {
//     document.querySelectorAll(".like-btn").forEach(button => {
//         button.addEventListener("click", function() {
//             const friendItem = this.closest(".friend-item");
//             const friendId = friendItem.getAttribute("data-id");
//
//             if (!friendId) {
//                 console.error("❌ friendId가 없습니다. HTML data-id 속성을 확인하세요.");
//                 alert("유효하지 않은 사용자입니다.");
//                 return;
//             }
//
//             const likeCountSpan = friendItem.querySelector(".likecount");
//
//             fetch(`/api/friends/${friendId}/like`, {
//                 method: "POST",
//                 headers: {[header]: token}
//             })
//                 .then(response => {
//                     if (!response.ok) throw new Error("서버 오류 발생");
//                     return response.json();
//                 })
//                 .then(updatedCount => {
//                     // 버튼 숨기고, 숫자 표시
//                     this.style.display = "none";
//                     likeCountSpan.textContent = `추천수 : ${updatedCount}`;
//                     likeCountSpan.style.display = "inline-block";
//                 })
//                 .catch(error => {
//                     alert("좋아요 처리 중 오류가 발생했습니다.");
//                     console.error(error);
//                 });
//         });
//     });
// });
// //채팅 버튼
// document.addEventListener("DOMContentLoaded", function () {
//     document.querySelectorAll(".chat-btn").forEach(btn => {
//         btn.addEventListener("click", function () {
//             const friendUserId = this.closest(".friend-item").getAttribute("data-userid");
//
//             if (!friendUserId) {
//                 console.error("❌ friendId가 없습니다. HTML data-userid 속성을 확인하세요.");
//                 alert("유효하지 않은 사용자입니다.");
//                 return;
//             }
//
//             fetch("/api/friends/chat", {
//                 method: "POST",
//                 headers: {
//                     "Content-Type": "application/json",
//                     [header]: token
//                 },
//                 body: JSON.stringify({ friendUserId: friendUserId })
//             })
//                 .then(response => {
//                     if (!response.ok) throw new Error("채팅방 생성 실패");
//                     return response.text();
//                 })
//                 .then(roomId => {
//                     // 채팅방 페이지로 이동
//                     window.location.href = `/chat-room/my-chat-list`;
//                 })
//                 .catch(err => {
//                     console.error(err);
//                     alert("채팅방 생성 중 오류가 발생했습니다.");
//                 });
//         });
//     });
// });
//
// // 삭제 버튼
// document.querySelectorAll(".delete-btn").forEach(button => {
//     button.addEventListener("click", function() {
//         const friendItem = this.closest(".friend-item");
//         const friendId = friendItem.getAttribute("data-id");
//
//         if (!friendId) return;
//
//         const confirmDelete = confirm("정말 이 친구를 삭제하시겠습니까?");
//         if (!confirmDelete) return;
//
//         fetch(`/api/friends/${friendId}/delete`, {
//             method: "DELETE",
//             headers: { [header]: token }
//         })
//             .then(res => {
//                 if (!res.ok) throw new Error("삭제 실패");
//                 return res.text();
//             })
//             .then(message => {
//                 alert(message);
//                 friendItem.remove(); // 화면에서 제거
//             })
//             .catch(err => {
//                 console.error(err);
//                 alert("친구 삭제 중 오류가 발생했습니다.");
//             });
//     });
// });