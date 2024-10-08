package book.book.book.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class savedBookCommentRequest {

    @Size(max = 1024)
    private String comment;
}
