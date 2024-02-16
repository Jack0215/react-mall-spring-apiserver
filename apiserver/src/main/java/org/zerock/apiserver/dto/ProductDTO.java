package org.zerock.apiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

//상품 등록과 조회할 때 씀
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private  Long pno;

    private  String pname;

    private int price;

    private String pdesc;

    private boolean delFlag;

    //업로드할 때
    @Builder.Default
    private List<MultipartFile> files = new ArrayList<>();

    @Builder.Default
    //조회할 때 씀, 이미 upload가 되어있음
    private List<String> uploadedFileNames= new ArrayList<>();



}
