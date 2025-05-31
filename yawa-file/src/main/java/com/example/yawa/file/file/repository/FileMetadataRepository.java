package com.example.yawa.file.file.repository;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.yawa.file.file.model.File;

@Repository
public class FileMetadataRepository {

  private final MongoClient mongo;
  private final String databaseName;

  @Autowired
  public FileMetadataRepository(
      MongoClient mongo,
      FileMetadataRepositoryProperties properties
  ) {
    this.mongo = mongo;
    this.databaseName = properties.databaseName;
  }

  public void create(File model) {
    collection()
        .insertOne(Assembler.disassemble(model));
  }

  public void createAll(Collection<File> models) {
    collection()
        .insertMany(Assembler.disassemble(models));
  }

  public void update(File model) {
    collection()
        .replaceOne(Filters.eq(model.getId()), Assembler.disassemble(model));
  }

  public void delete(File model) {
    collection()
        .deleteOne(Filters.eq(model.getId()));
  }

  public void replaceAll(Collection<File> actual, Collection<File> replacement) {
    List<WriteModel<Document>> requests = new ArrayList<>();
    actual.stream()
        .map(File::getId)
        .map(Filters::eq)
        .<DeleteOneModel<Document>>map(DeleteOneModel::new)
        .forEach(requests::add);
    replacement.stream()
        .map(Assembler::disassemble)
        .map(InsertOneModel::new)
        .forEach(requests::add);

    collection()
        .bulkWrite(requests);
  }

  public Optional<File> findById(String id) {
    Document document = collection()
        .find(Filters.eq(id))
        .first();

    return Optional.ofNullable(document)
        .map(Assembler::assemble);
  }

  public List<File> findAllByUserId(UUID userId) {
    MongoIterable<Document> documents = collection()
        .find(Filters.eq("user_id", Assembler.disassembleUserId(userId)));

    return Assembler.assemble(documents);
  }

  private MongoCollection<Document> collection() {
    return mongo.getDatabase(databaseName)
        .getCollection("file_metadata");
  }


  public static class FileMetadataRepositoryProperties {

    private final String databaseName;

    public FileMetadataRepositoryProperties(String databaseName) {
      this.databaseName = databaseName;
    }

  }

  private static class Assembler {

    static File assemble(Document document) {
      File model = new File();
      model.setId(document.getString("_id"));
      model.setUserId(assembleUserId(document.getString("user_id")));
      model.setName(document.getString("name"));
      model.setContentType(document.getString("content_type"));
      model.setUrl(assembleUrl(document.getString("url")));
      model.setUrlExpiredAt(assembleDate(document.getDate("url_expired_at")));
      model.setCreatedAt(assembleDate(document.getDate("created_at")));
      return model;
    }

    static List<File> assemble(MongoIterable<Document> documents) {
      List<File> models = new ArrayList<>();

      try (MongoCursor<Document> cursor = documents.iterator()) {
        while (cursor.hasNext()) {
          Document document = cursor.next();
          models.add(Assembler.assemble(document));
        }
      }

      return models;
    }

    static UUID assembleUserId(String userId) {
      return userId == null ? null : UUID.fromString(userId);
    }

    static URL assembleUrl(String url) {
      try {
        return new URL(url);
      } catch (Exception e) {
        throw new RuntimeException("Unable to assemble url: " + url, e);
      }
    }

    static OffsetDateTime assembleDate(Date date) {
      // BSON stores UTC datetime. https://www.mongodb.com/docs/manual/reference/method/Date
      return date == null ? null : OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }

    static Document disassemble(File model) {
      return new Document()
          .append("_id", model.getId())
          .append("user_id", disassembleUserId(model.getUserId()))
          .append("name", model.getName())
          .append("content_type", model.getContentType())
          .append("url", disassembleUrl(model.getUrl()))
          .append("url_expired_at", disassembleDate(model.getUrlExpiredAt()))
          .append("created_at", disassembleDate(model.getCreatedAt()));
    }

    static List<Document> disassemble(Collection<File> models) {
      return models.stream()
          .map(Assembler::disassemble)
          .collect(Collectors.toList());
    }

    static String disassembleUserId(UUID userId) {
      return userId == null ? null : userId.toString();
    }

    static String disassembleUrl(URL url) {
      return url.toString();
    }

    static Date disassembleDate(OffsetDateTime date) {
      return date == null ? null : Date.from(date.toInstant());
    }

  }

}
