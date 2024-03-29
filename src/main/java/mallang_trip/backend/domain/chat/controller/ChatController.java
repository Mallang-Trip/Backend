package mallang_trip.backend.domain.chat.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.chat.service.ChatService;
import mallang_trip.backend.domain.chat.service.ChatBlockService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.chat.dto.ChatMessageResponse;
import mallang_trip.backend.domain.chat.dto.ChatRoomBriefResponse;
import mallang_trip.backend.domain.chat.dto.ChatRoomDetailsResponse;
import mallang_trip.backend.domain.chat.dto.ChatRoomIdResponse;
import mallang_trip.backend.domain.user.dto.UserBriefResponse;
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
	private final ChatBlockService chatBlockService;

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

	@GetMapping("/party/{party_id}")
	@ApiOperation(value = "파티 채팅방 입장하기")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<ChatRoomIdResponse> enterPartyChatRoom(
		@PathVariable(value = "party_id") Long partyId) throws BaseException {
		return new BaseResponse<>(chatService.enterPartyChatRoom(partyId));
	}

	@DeleteMapping("/party/{chat_room_id}")
	@ApiOperation(value = "파티 공용 채팅방 강퇴하기")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> kickChatMember(
		@PathVariable(value = "chat_room_id") Long roomId,
		@RequestParam(value = "userId") Long userId) throws BaseException {
		chatService.kickChatMember(roomId, userId);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/block/{user_id}")
	@ApiOperation(value = "차단하기")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> blockUser(@PathVariable(value = "user_id") Long userId)
		throws BaseException {
		chatBlockService.save(userId);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/block/{user_id}")
	@ApiOperation(value = "차단취소")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> cancelBlock(@PathVariable(value = "user_id") Long userId)
		throws BaseException {
		chatBlockService.delete(userId);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/block")
	@ApiOperation(value = "차단한 유저 조회")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<List<UserBriefResponse>> getBlockingUser() throws BaseException {
		return new BaseResponse<>(chatBlockService.getBlockList());
	}

	@GetMapping("/messages/{chat_room_id}")
	@ApiOperation(value = "(관리자) 모든 채팅 내역 조회")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<ChatMessageResponse>> getEntireMessages(
		@PathVariable(value = "chat_room_id") Long roomId) throws BaseException {
		return new BaseResponse<>(chatService.getEntireMessages(roomId));
	}
}
