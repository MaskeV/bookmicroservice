package com.bookmgmt.bookservice.document;



import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;




@Document(indexName = "books" )
public class BookDocument {

	@Id
	private String id;

	// numeric copy of the MySQL id: used for stable sorting and for building the Book response
	@Field(type = FieldType.Integer)
	private Integer bookId;

	// title/author are searched as text, and also stored as ".keyword" so they can be sorted on
	@MultiField(mainField = @Field(type = FieldType.Text),
			otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String title;
	@MultiField(mainField = @Field(type = FieldType.Text),
			otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String author;
    @Field(type = FieldType.Text)
    private String description;
    @Field(type = FieldType.Keyword)
    private String category;
    @Field(type = FieldType.Keyword)
    private String publisher;
    @Field(type = FieldType.Integer)
    private Integer publicationYear;
    @Field(type = FieldType.Integer)
    private Integer quantity;
    
 

	@Field(type = FieldType.Keyword)
    private String language;
    @Field(type = FieldType.Boolean)
    private Boolean available;

    public BookDocument() {
    	
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getBookId() {
		return bookId;
	}

	public void setBookId(Integer bookId) {
		this.bookId = bookId;
	}



	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public Integer getPublicationYear() {
		return publicationYear;
	}

	public void setPublicationYear(Integer publicationYear) {
		this.publicationYear = publicationYear;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}
	
	 public Integer getQuantity() {
			return quantity;
	 }

	public void setQuantity(Integer quantity) {
			this.quantity = quantity;
	}
    

}