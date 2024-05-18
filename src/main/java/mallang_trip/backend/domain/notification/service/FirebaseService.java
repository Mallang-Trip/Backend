package mallang_trip.backend.domain.notification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;

import mallang_trip.backend.domain.notification.entity.Firebase;
import mallang_trip.backend.domain.notification.repository.FirebaseRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class FirebaseService{

    private final FirebaseMessaging firebaseMessaging;

    private final CurrentUserService currentUserService;
    private final FirebaseRepository firebaseRepository;

    public FirebaseService(@Value("${firebase.src}") String src, CurrentUserService currentUserService, FirebaseRepository firebaseRepository) {
        log.info("FirebaseService start");
        try{
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(src).getInputStream()))
                    .build();

            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
            firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);

            this.currentUserService = currentUserService;
            this.firebaseRepository = firebaseRepository;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Firebase Token 제거
     *
     */
    public void deleteToken(){
        User user = currentUserService.getCurrentUser();

        Optional<Firebase> firebase = firebaseRepository.findByUserAndTokenNotNull(user);
        if(firebase.isPresent()){
            firebase.get().setToken(null);
        } else {
            log.error("Firebase Token Not Found : {}", user.getId());
        }
    }

    /**
     * Firebase Token 등록
     */
    public void saveToken(String token) {
        User user = currentUserService.getCurrentUser();

        if(firebaseRepository.existsByUser(user)) {
            Firebase firebase = firebaseRepository.findByUser(user).get();
            firebase.setToken(token);
        } else {
            Firebase firebase = Firebase.builder()
                    .user(user)
                    .token(token)
                    .build();
            firebaseRepository.save(firebase);
        }
    }

    /**
     * Firebase Token 갱신
     * @param token
     */
    public void updateToken(String token) {
        User user = currentUserService.getCurrentUser();

        Optional<Firebase> firebase = firebaseRepository.findByUser(user);
        if(firebase.isPresent()){
            firebase.get().setToken(token);
        } else {
            log.error("Firebase Token Not Found : {}", user.getId());
        }
    }

    /**
     * Firebase push 알림 - 여러명
     *
     */
    @Async
    public void sendPushMessage(List<String> tokens, String title, String body) throws FirebaseMessagingException {

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .addAllTokens(tokens)
                .build();
        BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
    }

    /**
     * Firebase push 알림 - 단일
     *
     */
//    @Async
    public void sendPushMessage(String token, String title, String body) {
        try{
        Message message = Message.builder().setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        String response = firebaseMessaging.send(message);

    }
    catch (FirebaseMessagingException e) {
        log.error("Firebase Messaging Error : {}", e.getMessage());
    }
    }

}
