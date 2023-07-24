package mallang_trip.backend.controller;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.service.CommentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    
}
