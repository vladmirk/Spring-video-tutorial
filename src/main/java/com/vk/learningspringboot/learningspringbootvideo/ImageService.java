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
import org.springframework.data.repository.query.Param;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ImageRepository imageRepository;
    private final ResourceLoader resource;
    private final CounterService counterService;
    private final GaugeService gaugeService;
    private SimpMessagingTemplate simpMessagingTemplate;
    private UserRepository userRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository, ResourceLoader resource, CounterService counterService,
                        GaugeService gaugeService, SimpMessagingTemplate
                                simpMessagingTemplate, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.resource = resource;
        this.counterService = counterService;
        this.gaugeService = gaugeService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userRepository = userRepository;

        this.counterService.reset("files.uploaded");
        this.gaugeService.submit("files.uploaded.lastBytes", 0);
    }

    public Page<Image> findPage(Pageable pageable) {
        return imageRepository.findAll(pageable);
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
            imageRepository.save(new Image(file.getOriginalFilename(),
                    userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
            ));
            counterService.increment("files.uploaded");
            gaugeService.submit("files.uploaded.lastBytes", file.getSize());
            simpMessagingTemplate.convertAndSend("/topic/newImage", file.getOriginalFilename());
        }
    }

    @PreAuthorize("@imageRepository.findByName(#filename)?.user?.username == authentication?.name or hasRole('ADMIN')")
    public void deleteImage(@Param("filename") String name) throws IOException {
        final Image byName = imageRepository.findByName(name);
        imageRepository.delete(byName);
        Files.deleteIfExists(Paths.get(UPLOAD_ROOT, name));
        simpMessagingTemplate.convertAndSend("/topic/deleteImage", name);
    }


    @Bean
//    @Profile("dev")
    CommandLineRunner setUp(ImageRepository imageRepository, UserRepository userRepository) throws IOException {
        FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));
        Files.createDirectories(Paths.get(UPLOAD_ROOT));


        return (args) -> {

            User user1 = userRepository.save(new User("user1", "pass", "ROLE_USER"));
            User user2 = userRepository.save(new User("user2", "pass", "ROLE_USER", "ROLE_OPERATOR"));


            FileCopyUtils.copy("Test file", new FileWriter(UPLOAD_ROOT + "/test"));
            imageRepository.save(new Image("test", user1));

            FileCopyUtils.copy("Test file2", new FileWriter(UPLOAD_ROOT + "/test2"));
            imageRepository.save(new Image("test2", user1));

            FileCopyUtils.copy("Test file3", new FileWriter(UPLOAD_ROOT + "/test3"));
            imageRepository.save(new Image("test3", user2));
        };
    }
}
