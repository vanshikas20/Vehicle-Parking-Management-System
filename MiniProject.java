package dbms;

import java.sql.*;
import java.util.Scanner;

public class MiniProject {
	private static final String DB_URL = "jdbc:mysql://localhost:3306/dbms_mp";
	private static final String USER = "root";
	private static final String PASSWORD = "vanshika@20";

	public static void main(String[] args) {
		try {
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			Scanner scanner = new Scanner(System.in);
			int choice = 0;
			System.out.println("Welcome to the Parking System!");
			do {
				System.out.println("\nChoose an option:");
				System.out.println("1. Add a Customer");
				System.out.println("2. Add a Vehicle");
				System.out.println("3. Start Parking Session");
				System.out.println("4. End Parking Session");
				System.out.println("5. Exit");
				choice = scanner.nextInt();

				switch (choice) {
				case 1:
					addCustomer(conn, scanner);
					break;
				case 2:
					addVehicle(conn, scanner);
					break;
				case 3:
					startParkingSession(conn, scanner);
					break;
				case 4:
					endParkingSession(conn, scanner);
					break;
				case 5: {
					System.out.println("Exiting... Goodbye!");
					break;
				}
				default:
					System.out.println("Invalid choice. Try again.");
				}
			} while (choice != 5);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void addCustomer(Connection conn, Scanner scanner) throws SQLException {
		System.out.println("Enter customer name:");
		String name = scanner.next();
		System.out.println("Enter email:");
		String email = scanner.next();
		System.out.println("Enter phone number:");
		String phone = scanner.next();
		System.out.println("Enter membership ID (0 for NA,1 for Basic, 2 for Silver,3 for Gold,4 for Platinum etc.):");
		int membershipId = scanner.nextInt();

		String sql = "INSERT INTO customers (name, email, phone_number, membership_id,membership_status) VALUES (?, ?, ?, ?,?)";
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setString(2, email);
			stmt.setString(3, phone);
			stmt.setInt(4, membershipId);
			if(membershipId==0) {
				stmt.setString(5, "non-member");
			}
			else {
				stmt.setString(5, "member");
			}
			stmt.executeUpdate();
			System.out.println("Customer added successfully!");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void addVehicle(Connection conn, Scanner scanner) throws SQLException {
		System.out.println("Enter customer ID:");
		int customerId = scanner.nextInt();
		System.out.println("Enter license plate:");
		String licensePlate = scanner.next();
		System.out.println("Enter lot ID:");
		int lotid = scanner.nextInt();
		
		String sql = "INSERT INTO vehicles (license_plate, customer_id,lot_id) VALUES (?, ?,?)";
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, licensePlate);
			stmt.setInt(2, customerId);
			stmt.setInt(3,lotid);
			stmt.executeUpdate();
			System.out.println("Vehicle added successfully!");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void startParkingSession(Connection conn, Scanner scanner) throws SQLException {
		System.out.println("Enter vehicle ID:");
		int vehicleId = scanner.nextInt();
		System.out.println("Enter desired parking lot ID:");
		int lotId = scanner.nextInt();

		CallableStatement stmt = conn.prepareCall("{CALL StartParkingSession(?, ?)}");
		stmt.setInt(1, vehicleId);
		stmt.setInt(2, lotId);

		try {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString("message"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void endParkingSession(Connection conn, Scanner scanner) throws SQLException {
		System.out.println("Enter session ID to end parking:");
		int sessionId = scanner.nextInt();

		// Update exit time
		String updateExitTimeSQL = "UPDATE ParkingSessions SET exit_time = CURRENT_TIMESTAMP WHERE session_id = ?";
		try {
			PreparedStatement stmt = conn.prepareStatement(updateExitTimeSQL);
			stmt.setInt(1, sessionId);
			int rowsUpdated = stmt.executeUpdate();
			if (rowsUpdated > 0) {
				System.out.println("Exit time updated for session ID " + sessionId);
			} else {
				System.out.println("Invalid session ID.");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Calculate parking fee
		CallableStatement calcFeeStmt = conn.prepareCall("{CALL CalculateParkingFee(?)}");
		calcFeeStmt.setInt(1, sessionId);
		calcFeeStmt.execute();

		// Retrieve updated price
		String fetchPriceSQL = "SELECT price FROM ParkingSessions WHERE session_id = ?";
		try {
			PreparedStatement fetchPriceStmt = conn.prepareStatement(fetchPriceSQL);
			fetchPriceStmt.setInt(1, sessionId);
			try {
				ResultSet rs = fetchPriceStmt.executeQuery();
				if (rs.next()) {
					double price = rs.getDouble("price");
					System.out.println("Total parking fee for session ID " + sessionId + ": $" + price);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
