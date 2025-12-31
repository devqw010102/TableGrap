package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BookResponseDto {

    private Long bookId;    //예약 수정용
    private Long dinerId;   //예약 수정용

    private String dinerName;

    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime bookingDate;
    private Integer personnel;
    private String memberName;
    private Boolean success;
    //후기 수정 구현위해 dto에 reviewId추가, 화면 출력만을 위해 필요하므로 entity에 추가x
    private Long reviewId;

    public boolean cancelAllowed;
    public void setCancelAllowed(boolean cancelAllowed) {
        this.cancelAllowed = cancelAllowed;
    }
}