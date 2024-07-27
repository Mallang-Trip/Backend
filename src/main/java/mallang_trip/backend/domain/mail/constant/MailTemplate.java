package mallang_trip.backend.domain.mail.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum MailTemplate {

    PARTY_SEALED("email_template_party_complete.html", Arrays.asList("name", "party_name", "date","party_number","party_people_name","driver_name","url")),
    PARTY_CANCELED("email_template_party_cancel.html",Arrays.asList("name","reason","url")),
    PARTY_MODIFIED("email_template_course_modify.html",Arrays.asList("name","reason","url")),
    NOTIFICATION("email_template_notify.html",Arrays.asList("name","reason","url"));

    private final String templateFileName;

    private final List<String> templateVariables;

    public String getTemplatePath() {
        return  templateFileName;
    }
}
