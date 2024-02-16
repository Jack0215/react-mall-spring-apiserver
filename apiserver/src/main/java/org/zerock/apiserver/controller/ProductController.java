package org.zerock.apiserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.apiserver.dto.PageRequestDTO;
import org.zerock.apiserver.dto.PageResponseDTO;
import org.zerock.apiserver.dto.ProductDTO;
import org.zerock.apiserver.service.ProductService;
import org.zerock.apiserver.util.CustomFileUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final CustomFileUtil fileUtil;

    private final ProductService productService;

//    @PostMapping("/")
//    public Map<String, String> register(ProductDTO productDTO) {
//        log.info("register:"+productDTO);
//
//        List<MultipartFile> files = productDTO.getFiles();
//
//        List<String> uploadedFileNames = fileUtil.saveFiles(files);
//
//        productDTO.setUploadedFileNames(uploadedFileNames);
//
//        log.info(uploadedFileNames);
//        return Map.of("RESULT", "SUCCESS");
//    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<Resource> viewFilesGET(@PathVariable("fileName") String fileName){
        return fileUtil.getFile(fileName);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("/list")
    public PageResponseDTO<ProductDTO> list(PageRequestDTO pageRequestDTO){
        return productService.getList(pageRequestDTO);
    }

    @PostMapping("/")
    public Map<String, Long> register(ProductDTO productDTO) throws IOException {

        List<MultipartFile> files = productDTO.getFiles();

        List<String> uploadFileNames = fileUtil.saveFiles(files);

        productDTO.setUploadedFileNames(uploadFileNames);

        log.info(uploadFileNames);

        Long pno = productService.register(productDTO);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Map.of("result", pno);

    }

    @GetMapping("/{pno}")
    public ProductDTO read(@PathVariable("pno") Long pno){
        return productService.get(pno);
    }

    @PutMapping("/{pno}")
    public  Map<String, String> modify(@PathVariable Long pno, ProductDTO productDTO) throws IOException {
        //이미지 업로드를 해야함, 수정해서 업로드가 되는 애들은 아직 저장이 안됨
        //저장을 해야함
        productDTO.setPno(pno);

        //DB에 저장되어있는 파일
        ProductDTO oldProductDTO = productService.get(pno);

        //파일 업로드
        List<MultipartFile> files = productDTO.getFiles();
        List<String> currentuploadFileNames = fileUtil.saveFiles(files);

        //keep files, Str 타입임
        List<String> uploadedFileNames = productDTO.getUploadedFileNames();

        //기존에 저장된 파일들(DB에도 있는)에 새로운 파일을 저장
        if(currentuploadFileNames !=null && !currentuploadFileNames.isEmpty()){
            uploadedFileNames.addAll(currentuploadFileNames);
        }

        productService.modify(productDTO);

        List<String> oldFileNames = oldProductDTO.getUploadedFileNames();
        if(oldFileNames !=null && !oldFileNames.isEmpty()){
           List<String> removeFiles =  oldFileNames.stream().filter(fileName ->
                   !uploadedFileNames.contains(fileName)
            ).collect(Collectors.toList());
        fileUtil.deleteFiles(removeFiles);
        }

        return Map.of("RESULT","SUCCESS");
    }

    @DeleteMapping("/{pno}")
    public Map<String , String > remove(@PathVariable Long pno){
        List<String> oldFileNames = productService.get(pno).getUploadedFileNames();

        productService.remove(pno);

        fileUtil.deleteFiles(oldFileNames);

        return Map.of("RESULT","SUCESS");
    }
}
