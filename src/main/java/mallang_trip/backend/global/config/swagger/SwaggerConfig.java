package mallang_trip.backend.global.config.swagger;

import com.fasterxml.classmate.TypeResolver;
import java.util.ArrayList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Response;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    TypeResolver typeResolver = new TypeResolver();

    @Bean
    public Docket api() {
        ArrayList<Response> globalResponse = new ArrayList<>();
        globalResponse.add(new ResponseBuilder().code("500")
            .description("알 수 없는 오류.").build()
        );

        return new Docket(DocumentationType.OAS_30)
            .alternateTypeRules(AlternateTypeRules.newRule(typeResolver.resolve(Pageable.class),
                typeResolver.resolve(CustomPageable.class)))
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
            .paths(PathSelectors.any())
            .build()
            .useDefaultResponseMessages(false)
            .globalResponses(HttpMethod.GET, globalResponse)
            .globalResponses(HttpMethod.POST, globalResponse)
            .globalResponses(HttpMethod.DELETE, globalResponse)
            .globalResponses(HttpMethod.PUT, globalResponse)
            .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("Mallang Trip API")
            .description("말랑트립 API 명세서")
            .version("1.0")
            .build();
    }
}
