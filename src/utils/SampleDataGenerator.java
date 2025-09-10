package utils;

import models.Book;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample data generator for testing
 */
public class SampleDataGenerator {
    
    /**
     * Generate sample books for testing
     */
    public static List<Book> generateSampleBooks() {
        List<Book> books = new ArrayList<>();
        
        books.add(new Book("BOOK001", "Java: The Complete Reference", "Herbert Schildt", "Programming", 2020));
        books.add(new Book("BOOK002", "Clean Code", "Robert C. Martin", "Programming", 2008));
        books.add(new Book("BOOK003", "Design Patterns", "Gang of Four", "Programming", 1994));
        books.add(new Book("BOOK004", "Effective Java", "Joshua Bloch", "Programming", 2017));
        books.add(new Book("BOOK005", "Spring Boot in Action", "Craig Walls", "Programming", 2018));
        
        books.add(new Book("BOOK006", "Harry Potter and the Philosopher's Stone", "J.K. Rowling", "Fantasy", 1997));
        books.add(new Book("BOOK007", "To Kill a Mockingbird", "Harper Lee", "Classic", 1960));
        books.add(new Book("BOOK008", "1984", "George Orwell", "Dystopian", 1949));
        books.add(new Book("BOOK009", "Pride and Prejudice", "Jane Austen", "Romance", 1813));
        books.add(new Book("BOOK010", "The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925));
        
        books.add(new Book("BOOK011", "Introduction to Algorithms", "Thomas H. Cormen", "Computer Science", 2009));
        books.add(new Book("BOOK012", "Computer Networks", "Andrew S. Tanenbaum", "Computer Science", 2010));
        books.add(new Book("BOOK013", "Database System Concepts", "Abraham Silberschatz", "Database", 2019));
        books.add(new Book("BOOK014", "Operating System Concepts", "Abraham Silberschatz", "Operating Systems", 2018));
        books.add(new Book("BOOK015", "Software Engineering", "Ian Sommerville", "Software Engineering", 2015));
        
        return books;
    }
      /**
     * Initialize database with sample data
     */
    public static void initializeSampleData() {
        SimpleBookDAO bookDAO = new SimpleBookDAO();
        List<Book> existingBooks = bookDAO.loadBooks();
        
        // Only add sample data if database is empty
        if (existingBooks.isEmpty()) {
            List<Book> sampleBooks = generateSampleBooks();
            bookDAO.saveBooks(sampleBooks);
            System.out.println("Sample data initialized with " + sampleBooks.size() + " books.");
        } else {
            System.out.println("Database already contains " + existingBooks.size() + " books.");
        }
    }
}
