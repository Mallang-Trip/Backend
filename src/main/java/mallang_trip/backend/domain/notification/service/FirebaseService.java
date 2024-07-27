package mallang_trip.backend.domain.notification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.messaging.*;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import mallang_trip.backend.domain.notification.dto.FirebaseRequest;
import mallang_trip.backend.domain.notification.dto.FirebaseUpdateDeleteRequest;
import mallang_trip.backend.domain.notification.entity.Firebase;
import mallang_trip.backend.domain.notification.repository.FirebaseRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FirebaseService{

    private FirebaseMessaging firebaseMessaging;

    private final CurrentUserService currentUserService;
    private final FirebaseRepository firebaseRepository;

    @Value("${firebase.src}")
    private String src;

    @PostConstruct
    public void init(){
        log.info("FirebaseService start");
        try{
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(src).getInputStream()))
                    .build();

            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
            firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Firebase Token 제거
     *
     */
    public void deleteToken(FirebaseUpdateDeleteRequest request){
        User user = currentUserService.getCurrentUser();

        Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
        if(firebase.isPresent()){
            List<String> tokens = firebase.get().getTokens();
            tokens.remove(request.getFirebaseToken());
            firebase.get().changeTokens(tokens);
        } else {
            log.error("Firebase Token Not Found : {}", user.getId());
        }
    }

    /**
     * Firebase Token 추가 (등록)
     *
     */
    public void updateToken(FirebaseUpdateDeleteRequest request) {
        User user = currentUserService.getCurrentUser();
        Firebase firebase = firebaseRepository.findByUser(user).orElse(null);

        // 처음 등록하는 경우
        if(firebase == null){
            List<String> tokens = new ArrayList<>();
            tokens.add(request.getFirebaseToken());
            firebaseRepository.save(
				Firebase.builder()
                		.user(user)
                		.tokens(tokens)
                		.build()
			);
            return;
        }

        // 추가하는 경우
        List<String> tokens = firebase.getTokens();
        if(tokens == null){
            tokens = new ArrayList<>();
        }
        tokens.add(request.getFirebaseToken());
        firebase.changeTokens(tokens);
    }

    /**
     * Firebase push 알림 - 여러명
     *
     */
    @Async
    public void sendPushMessage(List<String> tokens, String title, String body, String url) {
        try{
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("tag",url)
                    .addAllTokens(tokens)
                    .build();
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
        }
        catch(FirebaseMessagingException e){
            log.error("Firebase Messaging Error : {}", e.getMessage());
        }
    }

    /**
     * Firebase push 알림 - 단일
     *
     */
    @Async
    public void sendPushMessage(String token, String title, String body, String url) {
        try{
            Message message = Message.builder().setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("tag",url)
                    .build();

            String response = firebaseMessaging.send(message);

        } catch (FirebaseMessagingException e) {
            log.error("Firebase Messaging Error : {}", e.getMessage());
        }
    }

    /**
     * Firebase push 알림 - 단일
     * 테스트용
     */
//    @Async
    public String sendPushMessageTest(String token, String title, String body, String url) {
        try{
        Message message = Message.builder().setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("tag",url)
                .build();

        String response = firebaseMessaging.send(message);

        return response;

        } catch (FirebaseMessagingException e) {
            log.error("Firebase Messaging Error : {}", e.getMessage());
            return "fail";
        }
    }

}
