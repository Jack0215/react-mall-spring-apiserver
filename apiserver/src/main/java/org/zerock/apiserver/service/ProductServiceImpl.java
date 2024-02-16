package org.zerock.apiserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.zerock.apiserver.domain.Product;
import org.zerock.apiserver.domain.ProductImage;
import org.zerock.apiserver.dto.PageRequestDTO;
import org.zerock.apiserver.dto.PageResponseDTO;
import org.zerock.apiserver.dto.ProductDTO;
import org.zerock.apiserver.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    @Override
    public PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO) {

        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage()-1,
                pageRequestDTO.getSize(),
                Sort.by("pno").descending());

        Page<Object[]> result = productRepository.selectList(pageable);

        //Object[0] => 0번째는 product, 1번째는 productImage

       List<ProductDTO> dtoList =  result.get().map(arr-> {
            ProductDTO productDTO = null;

           Product product = (Product) arr[0];
           ProductImage productImage = (ProductImage) arr[1];

           productDTO = ProductDTO.builder()
                   .pno(product.getPno())
                   .pname(product.getPname())
                   .pdesc(product.getPdesc())
                   .price(product.getPrice())
                   .build();

           String imageStr = productImage.getFileName();
           productDTO.setUploadedFileNames(List.of(imageStr));

            return productDTO;
        }).collect(Collectors.toList());

       long totalCount = result.getTotalElements();


        return  PageResponseDTO
                .<ProductDTO>withAll()
                .dtoList(dtoList)
                .total(totalCount)
                .pageRequestDTO(pageRequestDTO)
                .build();

    }

    @Override
    public Long register(ProductDTO productDTO) {

        Product product = dtoToEntity(productDTO);

        log.info("----------------");
        log.info(product);
        log.info(product.getImageList());

       Long pno = productRepository.save(product).getPno();
        return pno;
    }

    @Override
    public ProductDTO get(Long pno) {

        Optional<Product> result = productRepository.findById(pno);

        Product product = result.orElseThrow();

        return entityToDTO(product);
    }

    @Override
    public void modify(ProductDTO productDTO) {
        //조회를 함
        Optional<Product> result = productRepository.findById(productDTO.getPno());

        //끄집어냄
        Product product = result.orElseThrow();

        //변경 내용 반영
        product.changePrice(productDTO.getPrice());
        product.changeName(productDTO.getPname());
        product.changeDesc(productDTO.getPdesc());
        product.changeDel(productDTO.isDelFlag());

        //이미지 처리를 위해서 목록을 비워야함

        //이미지 목록 끄집어내기
        List<String> uploadFileNames = productDTO.getUploadedFileNames(); //이미지 저장이 끝난애들

        //근데 우리는 어떤게 저장되고 저장 안됐는지 알 수 없으므로 일단 다 지음
        product.clearList();

        if(uploadFileNames!=null && !uploadFileNames.isEmpty()){
            uploadFileNames.forEach(uploadName -> {
                product.addImageString(uploadName);
            });
        }
        //저장을 함
        productRepository.save(product);
    }

    @Override
    public void remove(Long pno) {
        productRepository.deleteById(pno);
    }

    private ProductDTO entityToDTO(Product product){

        ProductDTO productDTO = ProductDTO.builder()
                .pno(product.getPno())
                .pname(product.getPname())
                .pdesc(product.getPdesc())
                .price(product.getPrice())
                .delFlag(product.isDelFlag())
                .build();

        List<ProductImage> imageList = product.getImageList();

        if(imageList ==null|| imageList.isEmpty()){
            return productDTO;
        }

        List<String> fileNameList = imageList.stream().map(productImage ->
                productImage.getFileName()).toList();

        productDTO.setUploadedFileNames(fileNameList);
        return productDTO;
    }


    private Product dtoToEntity(ProductDTO productDTO){
        Product product = Product.builder()
                .pno(productDTO.getPno())
                .pname(productDTO.getPname())
                .pdesc(productDTO.getPdesc())
                .price(productDTO.getPrice())
                .build();

        List<String> uploadFileNames = productDTO.getUploadedFileNames();

        if(uploadFileNames == null || uploadFileNames.isEmpty()){
            return product;
        }
        uploadFileNames.forEach(fileName-> {
            product.addImageString(fileName);
        });


        return  product;

    }
}
