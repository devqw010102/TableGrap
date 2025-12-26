package com.example.demo.data.dto.admin;

import com.example.demo.data.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminBookDto {
    private Long id;
    private LocalDateTime createDate;
    private String dinerName;
    private LocalDateTime bookingDate;
    private Integer personnel;
    private String memberName;
    private Boolean success;

    public static AdminBookDto from(Book book) {
        return new AdminBookDto(
                book.getBookId(),
                book.getAddDate(),
                book.getDiner().getDinerName(),
                book.getBookingDate(),
                book.getPersonnel(),
                book.getMember().getName(),
                book.getSuccess()
        );
    }
}
