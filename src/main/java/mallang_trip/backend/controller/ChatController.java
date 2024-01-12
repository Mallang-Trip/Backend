package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.chat.ChatRoomBriefResponse;
import mallang_trip.backend.domain.dto.chat.ChatRoomDetailsResponse;
import mallang_trip.backend.domain.dto.chat.ChatRoomIdResponse;
import mallang_trip.backend.service.chat.ChatService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Chat API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

	private final ChatService chatService;

	@GetMapping("/groupChat")
	@ApiOperation(value = "그룹 채팅방 만들기")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<ChatRoomIdResponse> startGroupChat(
		@RequestParam(value = "userId") List<Long> userId,
		@RequestParam(value = "roomName") String roomName) throws BaseException {
		return new BaseResponse<>(chatService.startGroupChat(userId, roomName));
	}

	@GetMapping("/coupleChat")
	@ApiOperation(value = "1:1 채팅방 만들기")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<ChatRoomIdResponse> startCoupleChat(
		@RequestParam(value = "userId") Long userId) throws BaseException {
		return new BaseResponse<>(chatService.startCoupleChat(userId));
	}

	@PostMapping("/invite/{chat_room_id}")
	@ApiOperation(value = "그룹 채팅방 초대")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> invite(
		@PathVariable(value = "chat_room_id") Long chatRoomId,
		@RequestParam(value = "userId") List<Long> userId) throws BaseException {
		chatService.inviteToGroupChat(chatRoomId, userId);
		return new BaseResponse<>("성공");
	}

	@PutMapping("/groupChat/{chat_room_id}")
	@ApiOperation(value = "그룹 채팅방 이름 변경")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> changeGroupChatRoomName(
		@PathVariable(value = "chat_room_id") Long chatRoomId,
		@RequestParam(value = "room_name") String roomName) throws BaseException {
		chatService.changeGroupChatRoomName(chatRoomId, roomName);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/leave/{chat_room_id}")
	@ApiOperation(value = "채팅방 나가기")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> leave(
		@PathVariable(value = "chat_room_id") Long chatRoomId) throws BaseException {
		chatService.leaveChat(chatRoomId);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/list")
	@ApiOperation(value = "채팅방 리스트 조회")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<List<ChatRoomBriefResponse>> getChatRoomList() throws BaseException {
		return new BaseResponse<>(chatService.getChatRooms());
	}

	@GetMapping("/{chat_room_id}")
	@ApiOperation(value = "채팅방 상세조회")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<ChatRoomDetailsResponse> getChatRoomDetails(
		@PathVariable(value = "chat_room_id") Long chatRoomId) throws BaseException {
		return new BaseResponse<>(chatService.getChatRoomDetails(chatRoomId));
	}
}
