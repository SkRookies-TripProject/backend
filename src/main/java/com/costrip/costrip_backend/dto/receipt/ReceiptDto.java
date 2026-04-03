package com.costrip.costrip_backend.dto.receipt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class ReceiptDto {
    private String storeName;
    private String date;
    private List<ItemDto> items;
    private Integer subtotal;
    private Integer tax;
    private Integer total;

    @Getter @Setter @NoArgsConstructor
    public static class ItemDto {
        private String name;
        private Integer price;
    }
}