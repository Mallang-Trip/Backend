package mallang_trip.backend.domain.mail.service;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.domain.mail.constant.MailStatus;
import mallang_trip.backend.domain.mail.constant.MailTemplate;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.party.service.PartyMemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final PartyMemberService partyMemberService;

    private final JavaMailSender javaMailSender; // 이메일 전송을 위한 JavaMailSender

    private final SpringTemplateEngine templateEngine; // 이메일 템플릿을 사용하기 위한 SpringTemplateEngine


    @Value("${spring.mail.username}")
    private String fromAddress; // application.properties에 등록한 이메일 username


    /**
     * 이메일 전송 메서드 (private)
     * @param mailTemplate
     * @param emailAddress 수신자 이메일 주소
     * @param name
     * @param mailContent
     * @throws MessagingException
     */

    private void sendEmail(MailTemplate mailTemplate,String emailAddress, String name,HashMap<String,String>mailContent) throws MessagingException, UnsupportedEncodingException {
        // 이메일 전송 로직

        Context context = new Context(); // template용 context

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        helper.setSubject(mailContent.get("title")); // 이메일 제목
        helper.setTo(emailAddress); // 수신자 이메일 주소
        helper.setFrom(fromAddress.concat("@mallangtrip.com"),"말랑트립");

        mailTemplate.getTemplateVariables().stream().forEach(variable->{
            if(variable.equals("name"))
            {
                context.setVariable("name",name);
            }
            else{
                context.setVariable(variable,mailContent.get(variable));
            }
        });

        String html=templateEngine.process(mailTemplate.getTemplatePath(),context);
        helper.setText(html,true); // true : html 형식으로 보내기 - 템플릿 사용해서

        javaMailSender.send(message);

    }


    /**
     * 파티원들에게 이메일 전송
     * @param party
     * @param mailStatus 예약완료, 취소, 변경 등
     * @param reason 세부 내용
     */
    @Async
    public void sendEmailParty(Party party, MailStatus mailStatus,String reason) {
        StringBuilder peopleNameBuilder = new StringBuilder();
        HashMap<String,String> mailContents= new HashMap<>();

        // 예약 완료일 경우, 전체 party의 인원 수와 이름 모두를 알아야 함.
        if (mailStatus.equals(MailStatus.SEALED)) {
            List<PartyMember> members = partyMemberService.getMembers(party);
            for (PartyMember member : members) {
                peopleNameBuilder.append(member.getUser().getName());
            }
            mailContents.put("party_name",party.getCourse().getName());
            StringBuilder date = new StringBuilder();
            date.append(party.getStartDate().toString());
            date.append(" ~ ");
            date.append(party.getEndDate().toString());
            mailContents.put("date",date.toString());
            mailContents.put("party_number",Integer.toString(partyMemberService.getTotalHeadcount(party)));
            mailContents.put("party_people_name",peopleNameBuilder.toString());
            mailContents.put("driver_name",party.getDriver().getUser().getName());

            mailContents.put("title","확정된 파티 안내드립니다.");
        }
        else if(mailStatus.equals(MailStatus.CANCELLED)){
            mailContents.put("reason",reason);
            mailContents.put("title","파티 취소 안내드립니다.");
        }
        else{
            mailContents.put("reason",reason);
            mailContents.put("title","파티 코스 수정 안내드립니다.");
        }

        partyMemberService.getMembers(party).stream()
                .forEach(member -> {
                    try {
                        if(mailStatus.equals(MailStatus.SEALED)){
                            sendEmail(MailTemplate.PARTY_SEALED,member.getUser().getEmail(),member.getUser().getName(),mailContents);
                        }
                        else if(mailStatus.equals(MailStatus.CANCELLED)){
                            sendEmail(MailTemplate.PARTY_CANCELED,member.getUser().getEmail(),member.getUser().getName(),mailContents);
                        }
                        else {
                            sendEmail(MailTemplate.PARTY_MODIFIED,member.getUser().getEmail(),member.getUser().getName(),mailContents);
                        }

                    } catch (MessagingException | UnsupportedEncodingException e) {
                        log.info("이메일 전송 실패 : User email : {} Error: {}", member.getUser().getEmail(), e.getMessage());
                    }
                });
    }
}
