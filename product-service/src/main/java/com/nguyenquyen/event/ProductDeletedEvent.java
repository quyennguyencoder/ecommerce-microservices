package com.nguyenquyen.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDeletedEvent {
    private String id;
}
