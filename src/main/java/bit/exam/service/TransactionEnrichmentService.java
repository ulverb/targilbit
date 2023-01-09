package bit.exam.service;

import bit.exam.model.IpData;
import bit.exam.redis.IRedisRepository;
import bit.exam.redis.RedisUtils;
import bit.exam.utils.JsonHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class TransactionEnrichmentService {

    private RestTemplate restTemplate;
    private IRedisRepository redisRepository;

    private static final String ENRICHED_FILES_PATH = "src/main/resources/enrichedfiles";
    private static final String SAVED_FILES_PATH = "src/main/resources/savedfiles";



    @Autowired
    public TransactionEnrichmentService(@Qualifier("restTemplate") RestTemplate restTemplate, IRedisRepository redisRepository) {
        this.restTemplate = restTemplate;
        this.redisRepository = redisRepository;
    }

    public void proceedFile(MultipartFile file) throws IOException {

        Path uploadPath = Paths.get(SAVED_FILES_PATH);
        Path filePath = uploadPath.resolve(file.getOriginalFilename());

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Could not save file: " + file.getName(), ioe);
        }

        readFile(file.getOriginalFilename(), filePath);
    }

    public void readFile(String fileName, Path filePath) throws IOException {
        Path uploadPath = Paths.get(ENRICHED_FILES_PATH);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path enrichedFilePath = uploadPath.resolve(fileName);
        File enrichedFile = new File(enrichedFilePath.toString());

        if (enrichedFile.exists()) {
            enrichedFile.delete();
            enrichedFile.createNewFile();
        }

        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {
            List<String> headers = new ArrayList<>(parser.getHeaderNames());
            headers.add("type");
            headers.add("location");

            try (CSVPrinter printer = new CSVPrinter(new FileWriter(enrichedFile),CSVFormat.DEFAULT
                    .withHeader(headers.toArray(new String[7])))) {

                for (final CSVRecord record : parser) {
                    final String ip = record.get("ip");
                    List values = record.stream().collect(Collectors.toList());

                    String jsonString = createEnrichment(ip);

                    IpData ipData = JsonHandlerUtils.convertJsonStringToObject(jsonString, IpData.class);
                    values.add(ipData.getType());
                    values.add(ipData.getLocation());

                    printer.printRecord(values);
                }
            }
        }
    }

    public String createEnrichment(String ip){

        String jsonString = redisRepository.getIpStackInfoByIp(ip);

        if(jsonString.isEmpty()){

            String resourceUrl  = "http://api.ipstack.com/" + ip + "?access_key=78dee0de7e165fc74983abfe2faa986b";
            ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.info("Failed to refresh ipstack : {}", response.getStatusCodeValue());
                throw new HttpClientErrorException(response.getStatusCode());
            }

            jsonString = response.getBody();

            redisRepository.createIpStackInfo(ip, RedisUtils.buildUserEvent(ip, jsonString));
        }

        return jsonString;
    }

    public Resource getEnrichedFile(String fileName) throws IOException {
        Path uploadPath = Paths.get(ENRICHED_FILES_PATH);
        Path enrichedFilePath = uploadPath.resolve(fileName);

        if (enrichedFilePath != null) {
            return new UrlResource(enrichedFilePath.toUri());
        }

        return null;
    }
}
