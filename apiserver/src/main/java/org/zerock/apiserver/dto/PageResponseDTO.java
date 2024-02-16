package org.zerock.apiserver.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class PageResponseDTO<E> {
    private List<E> dtoList;

    private  List<Integer> pageNumList;

    private PageRequestDTO pageRequestDTO;

    private boolean prev, next;

    private int totalCount, prevPage, nextPage, totalPage, current;

    @Builder (builderMethodName =  "withAll")
    public PageResponseDTO(List<E> dtoList, PageRequestDTO pageRequestDTO, long total){

        this.dtoList = dtoList;
        this.pageRequestDTO = pageRequestDTO;
        this.totalCount = (int)total;

        //끝페이지 end
        int end = (int) (Math.ceil(pageRequestDTO.getPage()/10.0))*10;

        int start = end - 9;

        //진짜 마지막
        int last = (int)(Math.ceil(totalCount/(double)pageRequestDTO.getSize()));

        end = end > last ? last : end;

        this.prev = start > 1;

        //다음
        this.next = totalCount > end * pageRequestDTO.getSize();

        //숫자 리스트
        this.pageNumList = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());

        //이 전 페이지 번호
        this.prevPage = prev ? start - 1:0;

        //이 다음 페이지 번호
        this.nextPage = next ? end + 1 : 0;

        this.current = pageRequestDTO.getPage();

    }
}
