package com.vk.learningspringboot.learningspringbootvideo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by vkutn on 20/12/2017.
 */
@Service
public class ImageService {

    private static final String UPLOAD_ROOT = "upload-dir";
    private final ImageRepository repository;
    private final ResourceLoader resource;
    private final CounterService counterService;
    private final GaugeService gaugeService;
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public ImageService(ImageRepository imageRepository, ResourceLoader resource, CounterService counterService, GaugeService gaugeService, SimpMessagingTemplate
            simpMessagingTemplate) {
        this.repository = imageRepository;
        this.resource = resource;
        this.counterService = counterService;
        this.gaugeService = gaugeService;
        this.simpMessagingTemplate = simpMessagingTemplate;

        this.counterService.reset("files.uploaded");
        this.gaugeService.submit("files.uploaded.lastBytes", 0);
    }

    public Page<Image> findPage(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Resource findOneImage(String name) {
        String path = UPLOAD_ROOT + "/" + name;
        Resource file = resource.getResource("file:" + path);
        if (!file.exists())
            System.out.println("File Doesn't exist: " + path);
        return file;
    }

    public void createImage(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            Files.copy(file.getInputStream(), Paths.get(UPLOAD_ROOT, file.getOriginalFilename()));
            repository.save(new Image(file.getOriginalFilename()));
            counterService.increment("files.uploaded");
            gaugeService.submit("files.uploaded.lastBytes", file.getSize());
            simpMessagingTemplate.convertAndSend("/topic/newImage", file.getOriginalFilename());
        }
    }

    public void deleteImage(String name) throws IOException {
        final Image byName = repository.findByName(name);
        repository.delete(byName);
        Files.deleteIfExists(Paths.get(UPLOAD_ROOT, name));
        simpMessagingTemplate.convertAndSend("/topic/deleteImage", name);
    }


    @Bean

        //    @Profile("dev")
    CommandLineRunner setUp(ImageRepository repository) throws IOException {
        FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));
        Files.createDirectories(Paths.get(UPLOAD_ROOT));

        return (args) -> {
            FileCopyUtils.copy("Test file", new FileWriter(UPLOAD_ROOT + "/test"));
            repository.save(new Image("test"));

            FileCopyUtils.copy("Test file2", new FileWriter(UPLOAD_ROOT + "/test2"));
            repository.save(new Image("test2"));

            FileCopyUtils.copy("Test file3", new FileWriter(UPLOAD_ROOT + "/test3"));
            repository.save(new Image("test3"));
        };
    }
}
