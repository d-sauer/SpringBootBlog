package com.davorsauer.commons;

import com.davorsauer.domain.ContentMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class ContentMetadataUtilsTest implements Logger {

    private ContentMetadata metadata = new ContentMetadata();

    @Before
    public void setup() {
        metadata.setDescription("Custom description.....\nbut with additional line!");

        Date date = new Date();
        date.setTime(1432159200000L);

        metadata.setPublishDate(date);
        metadata.setPublished(true);
        HashSet<String> tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");
        metadata.setTags(tags);
        metadata.setTitle("Custom title");
    }

    @Test
    public void write() {
        String strMetadata = ContentMetadataUtils.writeMetadata(metadata);
        info("Content Metadata: \n" + strMetadata);

        assertThat(strMetadata, containsString("Custom description"));
        assertThat(strMetadata, containsString("tag1"));
        assertThat(strMetadata, containsString("tag2"));
        assertThat(strMetadata, containsString("Custom title"));
    }

    @Test
    public void read() {
        String metadata = "Description:Custom description.....\n" +
                "but little bit longer!\n" +
                "Tags:tag1,tag2\n" +
                "Title:Custom title\n" +
                "PublishDate:2015-05-21";

        ContentMetadata contentMetadata = ContentMetadataUtils.readMetadata(metadata);

        assertThat(contentMetadata.getDescription(), containsString("Custom description"));
        assertThat(contentMetadata.getDescription(), containsString("but little bit longer"));
        assertThat(contentMetadata.getTags(), hasItems("tag1", "tag2"));
        assertThat(contentMetadata.getTitle(), is("Custom title"));

        Date date = new Date();
        date.setTime(1432159200000L);

        assertThat(contentMetadata.getPublishDate(), is(date));
    }

    @Test
    public void readInputStream() throws IOException {
        String metadata = ContentMetadataUtils.writeMetadata(this.metadata);

        Path metadataFilePath = Paths.get(this.getClass().getClassLoader().getResource(".").getPath(), "metadata");
        info("Write metadata file: " + metadataFilePath.toString());

        try (BufferedWriter writer = Files.newBufferedWriter(metadataFilePath)) {
            writer.write(metadata);
        }

        // read files as stream:
        InputStream is = new FileInputStream(new File(metadataFilePath.toString()));
        ContentMetadata contentMetadata = ContentMetadataUtils.readMetadata(is);

        assertThat(contentMetadata.getDescription(), containsString("Custom description"));
        assertThat(contentMetadata.getDescription(), containsString("but with additional line"));
        assertThat(contentMetadata.getTags(), hasItems("tag1", "tag2"));
        assertThat(contentMetadata.getTitle(), is("Custom title"));

        Date date = new Date();
        date.setTime(1432159200000L);

        assertThat(contentMetadata.getPublishDate(), is(date));
    }

}
