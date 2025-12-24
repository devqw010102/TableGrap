package com.example.demo.data.dto.owner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDto {
  private Long id;

  @NotBlank(message = "이름을 입력하세요")
  @Size(max = 10, message = "이름은 10자 이내여야 합니다.")
  @Pattern(regexp = "^[가-힣]{2,10}$", message = "이름은 한글만 가능합니다.")
  private String name;

  @NotBlank(message = "아이디를 입력하새요")
  @Size(min = 3, max = 12, message = "아이디는 3~12자 사이여야 합니다.")
  @Pattern(regexp = "^[a-zA-Z0-9]{3,12}", message = "아이디는 영문자와 숫자를 포함한 3~12자 이내여야 합니다.")
  private String username;
  @NotBlank(message = "비밀번호를 입력하세요")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
          message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~16자여야 합니다")
  private String password;
  @NotBlank(message = "비밀번호를 확인해주세요")
  private String passwordConfirm;
  @NotBlank(message = "이메일을 입력하세요")
  @Pattern(regexp = "^[a-zA-Z0-9]{2,}@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
          message = "올바른 이메일 형식이 아닙니다.")
  private String email;
  @Pattern(regexp = "^$|^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "(-) 제외 번호만 입력해주세요.")
  private String phone;
  @NotBlank(message = "사업자 등록번호를 입력하세요")
  @Pattern(regexp = "^\\d{10}$",
          message = "사업자 등록번호는 숫자 10자리여야 합니다.")
  private String businessNum; //조회용도
  private String dinerName; //조회용도
  //entity에 저장하지 않음 식당 조회용도
  private List<String> dinerNames;
}
