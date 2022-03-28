package com.alkemy.ong.web.utils;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PageDTO<T> {
    private List<T> body;
    private String previuosPage;
    private String nextPage;
}
