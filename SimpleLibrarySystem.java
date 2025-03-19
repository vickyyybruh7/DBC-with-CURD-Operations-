import java.sql.*;
import java.util.Scanner;

public class SimpleLibrarySystem {
    // Database connection parameters
    private static final String URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root"; // Change as needed
    private static final String PASSWORD = "1"; // Change as needed
    
    // User information
    private static final String CURRENT_DATE = "2025-03-13 12:49:35";
    private static final String CURRENT_USER = "vignesh_g";
    
    public static void main(String[] args) {
        displayHeader();
        
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        
        while (!exit) {
            displayMenu();
            int choice = getIntInput(scanner);
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1: addBook(scanner); break;
                case 2: viewAllBooks(); break;
                case 3: addMember(scanner); break;
                case 4: viewAllMembers(); break;
                case 5: issueBook(scanner); break;
                case 6: returnBook(scanner); break;
                case 7: viewAllLoans(); break;
                case 0: 
                    exit = true;
                    System.out.println("Thank you for using the Library System!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        
        scanner.close();
    }
    
    private static void displayHeader() {
        System.out.println("=================================");
        System.out.println("  SIMPLE LIBRARY SYSTEM");
        System.out.println("=================================");
        System.out.println("Date: " + CURRENT_DATE);
        System.out.println("User: " + CURRENT_USER);
        System.out.println("=================================");
    }
    
    private static void displayMenu() {
        System.out.println("\nMAIN MENU:");
        System.out.println("1. Add Book");
        System.out.println("2. View All Books");
        System.out.println("3. Add Member");
        System.out.println("4. View All Members");
        System.out.println("5. Issue Book");
        System.out.println("6. Return Book");
        System.out.println("7. View All Loans");
        System.out.println("0. Exit");
        System.out.print("\nEnter your choice: ");
    }
    
    private static int getIntInput(Scanner scanner) {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.next(); // Clear invalid input
            return -1;
        }
    }
    
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // Book Operations
    
    private static void addBook(Scanner scanner) {
        System.out.println("\n-- ADD BOOK --");
        
        System.out.print("Enter title: ");
        String title = scanner.nextLine();
        
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO books (title, author, available) VALUES (?, ?, TRUE)")) {
            
            stmt.setString(1, title);
            stmt.setString(2, author);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                System.out.println("Book added successfully!");
            }
            
        } catch (SQLException e) {
            System.out.println("Error adding book: " + e.getMessage());
        }
    }
    
    private static void viewAllBooks() {
        System.out.println("\n-- ALL BOOKS --");
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
            
            if (!rs.isBeforeFirst()) {
                System.out.println("No books found in the library.");
                return;
            }
            
            System.out.printf("%-5s %-30s %-20s %-10s\n", 
                    "ID", "Title", "Author", "Available");
            System.out.println("------------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-5d %-30s %-20s %-10s\n", 
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBoolean("available") ? "Yes" : "No");
            }
            
        } catch (SQLException e) {
            System.out.println("Error viewing books: " + e.getMessage());
        }
    }
    
    // Member Operations
    
    private static void addMember(Scanner scanner) {
        System.out.println("\n-- ADD MEMBER --");
        
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO members (name, email) VALUES (?, ?)")) {
            
            stmt.setString(1, name);
            stmt.setString(2, email);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                System.out.println("Member added successfully!");
            }
            
        } catch (SQLException e) {
            System.out.println("Error adding member: " + e.getMessage());
        }
    }
    
    private static void viewAllMembers() {
        System.out.println("\n-- ALL MEMBERS --");
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM members")) {
            
            if (!rs.isBeforeFirst()) {
                System.out.println("No members found.");
                return;
            }
            
            System.out.printf("%-5s %-30s %-30s\n", "ID", "Name", "Email");
            System.out.println("------------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-5d %-30s %-30s\n", 
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"));
            }
            
        } catch (SQLException e) {
            System.out.println("Error viewing members: " + e.getMessage());
        }
    }
    
    // Loan Operations
    
    private static void issueBook(Scanner scanner) {
        System.out.println("\n-- ISSUE BOOK --");
        
        System.out.print("Enter book ID: ");
        int bookId = getIntInput(scanner);
        scanner.nextLine();
        
        System.out.print("Enter member ID: ");
        int memberId = getIntInput(scanner);
        scanner.nextLine();
        
        try (Connection conn = getConnection()) {
            // Check if book is available
            try (PreparedStatement checkStmt = conn.prepareStatement(
                     "SELECT available FROM books WHERE id = ?")) {
                
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next() || !rs.getBoolean("available")) {
                    System.out.println("Book not found or not available.");
                    return;
                }
            }
            
            // Check if member exists
            try (PreparedStatement checkStmt = conn.prepareStatement(
                     "SELECT id FROM members WHERE id = ?")) {
                
                checkStmt.setInt(1, memberId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Member not found.");
                    return;
                }
            }
            
            conn.setAutoCommit(false);
            
            try {
                // Insert loan record
                try (PreparedStatement insertStmt = conn.prepareStatement(
                         "INSERT INTO loans (book_id, member_id, loan_date) VALUES (?, ?, NOW())")) {
                    insertStmt.setInt(1, bookId);
                    insertStmt.setInt(2, memberId);
                    insertStmt.executeUpdate();
                }
                
                // Update book availability
                try (PreparedStatement updateStmt = conn.prepareStatement(
                         "UPDATE books SET available = FALSE WHERE id = ?")) {
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();
                }
                
                conn.commit();
                System.out.println("Book issued successfully!");
                
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error issuing book: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private static void returnBook(Scanner scanner) {
        System.out.println("\n-- RETURN BOOK --");
        
        System.out.print("Enter book ID: ");
        int bookId = getIntInput(scanner);
        scanner.nextLine();
        
        try (Connection conn = getConnection()) {
            // Check if book is on loan
            int loanId = -1;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(
                     "SELECT id FROM loans WHERE book_id = ? AND return_date IS NULL")) {
                
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Book not found or not currently on loan.");
                    return;
                }
                
                loanId = rs.getInt("id");
            }
            
            conn.setAutoCommit(false);
            
            try {
                // Update loan record
                try (PreparedStatement updateStmt = conn.prepareStatement(
                         "UPDATE loans SET return_date = NOW() WHERE id = ?")) {
                    updateStmt.setInt(1, loanId);
                    updateStmt.executeUpdate();
                }
                
                // Update book availability
                try (PreparedStatement updateStmt = conn.prepareStatement(
                         "UPDATE books SET available = TRUE WHERE id = ?")) {
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();
                }
                
                conn.commit();
                System.out.println("Book returned successfully!");
                
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error returning book: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private static void viewAllLoans() {
        System.out.println("\n-- ALL LOANS --");
        
        String sql = "SELECT l.id, b.title, m.name, l.loan_date, l.return_date " +
                     "FROM loans l " +
                     "JOIN books b ON l.book_id = b.id " +
                     "JOIN members m ON l.member_id = m.id " +
                     "ORDER BY l.loan_date DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (!rs.isBeforeFirst()) {
                System.out.println("No loan records found.");
                return;
            }
            
            System.out.printf("%-5s %-30s %-20s %-20s %-20s\n", 
                    "ID", "Book Title", "Member Name", "Loan Date", "Return Date");
            System.out.println("-------------------------------------------------------------------------------------");
            
            while (rs.next()) {
                String returnDate = rs.getTimestamp("return_date") != null ? 
                                   rs.getTimestamp("return_date").toString() : "Not returned";
                
                System.out.printf("%-5d %-30s %-20s %-20s %-20s\n", 
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("name"),
                        rs.getTimestamp("loan_date"),
                        returnDate);
            }
            
        } catch (SQLException e) {
            System.out.println("Error viewing loans: " + e.getMessage());
        }
    }
}