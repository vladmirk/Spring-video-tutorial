package com.vk.learningspringboot.learningspringbootvideo;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by vkutn on 20/12/2017.
 */
public interface ImageRepository extends PagingAndSortingRepository<Image, Long> {

    public Image findByName(String name);
}
