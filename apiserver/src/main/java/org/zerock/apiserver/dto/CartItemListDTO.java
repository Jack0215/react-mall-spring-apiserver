package org.zerock.apiserver.dto;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class CartItemListDTO {

    private Long cino;
    private int qty;
    private String pname;
    private int price;
    private  String imgeFile;

    public CartItemListDTO(Long cino, int qty, String pname, int price, String imgeFile) {
        this.cino = cino;
        this.qty = qty;
        this.pname = pname;
        this.price = price;
        this.imgeFile = imgeFile;
    }
}
