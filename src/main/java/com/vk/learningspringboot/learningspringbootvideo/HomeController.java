package com.vk.learningspringboot.learningspringbootvideo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Created by vkutn on 21/12/2017.
 */
@Controller
public class HomeController {
    public static final String BASE_PATH = "/images";
    public static final String FILENAME = "{filename:.+}";
    private final ImageService service;

    @Autowired
    public HomeController(ImageService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/" + FILENAME + "/raw")
    @ResponseBody
    public ResponseEntity<?> oneRawImage(@PathVariable String filename) {
        try {
            Resource file = service.findOneImage(filename);
            return ResponseEntity.ok().contentLength(file.contentLength()).contentType(MediaType.IMAGE_JPEG).body(new InputStreamResource(file.getInputStream()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Couldn't find image : " + filename + " => " + e.getMessage());
        }
    }

    @RequestMapping(value = "/")
    public String index(Model model, Pageable pageable) {
        final Page<Image> page = service.findPage(pageable);
        model.addAttribute("page", page);
        if (page.hasPrevious())
            model.addAttribute("prev", pageable.previousOrFirst());
        if (page.hasNext())
            model.addAttribute("next", pageable.next());
        return "index";
    }

    @RequestMapping(method = RequestMethod.POST, value = BASE_PATH)
    public String createFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            service.createImage(file);
            redirectAttributes.addFlashAttribute("flash.message", "Successfully uploaded " + file.getOriginalFilename());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("flash.message", "Failed to upload " + file.getOriginalFilename() + " => " + e.getMessage());
        }
        return "redirect:/";
    }

    @RequestMapping(method = RequestMethod.DELETE, value = BASE_PATH + "/" + FILENAME)
    public String deleteFile(@PathVariable String filename, RedirectAttributes redirectAttributes) {
        try {
            service.deleteImage(filename);
            redirectAttributes.addFlashAttribute("flash.message", "Successfully deleted" + filename);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("flash.message", "Failed to delete" + filename + " =>" + e.getMessage());
        }
        return "redirect:/";
    }

}
