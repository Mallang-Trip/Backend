package mallang_trip.backend.domain.gmail.service;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.service.PartyMemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailService {

    private final PartyMemberService partyMemberService;

    private final JavaMailSender javaMailSender; // 이메일 전송을 위한 JavaMailSender

    private final SpringTemplateEngine templateEngine; // 이메일 템플릿을 사용하기 위한 SpringTemplateEngine


    @Value("${spring.mail.username}")
    private String fromAddress; // application.properties에 등록한 이메일 username


    /**
     * 이메일 전송 메서드 (private)
     * @param emailAddress 수신자 이메일 주소
     * @param title 이메일 제목
     * @param content 이메일 내용
     * @throws MessagingException
     */

    private void sendEmail(String emailAddress, String title, String content,Boolean ready) throws MessagingException, UnsupportedEncodingException {
        // 이메일 전송 로직

        //Context context; // template용 context

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        helper.setSubject(title); // 이메일 제목
        helper.setTo(emailAddress); // 수신자 이메일 주소
        helper.setFrom(fromAddress.concat("@gmail.com"),"말랑트립-MallangTrip");


        // html 형식으로 이메일을 보내기 위해 Thymeleaf 템플릿 엔진을 사용
//        if(ready)
//        {
//            // 예약 완료용 템플릿
        //        context=new Context();
//        }
//        else{
//            // 예약 취소용 템플릿
        //        context=new Context();
//        }
//        context.setVariable("title",title); // template 내의 title 변수에 title 값 주입
//        String html=templateEngine.process("email-template",context);
//        helper.setText(html,true); // true : html 형식으로 보내기 - 템플릿 사용해서


        helper.setText("test email",false);
        javaMailSender.send(message);

    }


    /**
     * 파티원들에게 이메일 전송
     * @param party
     * @param ready
     */
    public void sendEmailParty(Party party, Boolean ready) {
        partyMemberService.getMembers(party).stream()
                .forEach(member -> {
                    try {
                        if(ready){
                            sendEmail(member.getUser().getEmail(), "여행 준비 완료", "여행 준비가 완료되었습니다. 여행을 시작해주세요!", ready);
                        }
                        else{
                            sendEmail(member.getUser().getEmail(), "예약이 취소되었습니다.", "예약이 취소되었습니다. 다음 기회에 다시 참여해주세요!", ready);
                        }

                    } catch (MessagingException | UnsupportedEncodingException e) {
                        log.info("이메일 전송 실패 : User email : {} Error: {}", member.getUser().getEmail(), e.getMessage());
                    }
                });
    }
}
