# Library Management System - Hệ thống Quản lý Thư viện qua Mạng

## Mô tả
Đây là một hệ thống quản lý thư viện được xây dựng bằng Java TCP Socket, Java Swing GUI và lưu trữ dữ liệu JSON. Hệ thống hỗ trợ nhiều client kết nối đồng thời và thực hiện các thao tác CRUD cho sách.

## Tính năng
- ✅ **CRUD Operations**: Thêm, sửa, xóa sách
- ✅ **Search & Filter**: Tìm kiếm sách theo tên, tác giả, thể loại
- ✅ **Borrow/Return**: Mượn và trả sách
- ✅ **Multi-client Support**: Hỗ trợ nhiều client kết nối đồng thời
- ✅ **Thread-safe**: Xử lý an toàn với concurrent access
- ✅ **GUI Interface**: Giao diện Java Swing thân thiện
- ✅ **Data Persistence**: Lưu trữ dữ liệu trong JSON file

## Cấu trúc Project
```
src/
├── models/
│   ├── Book.java          # Model dữ liệu sách
│   ├── Request.java       # Model request từ client
│   └── Response.java      # Model response từ server
├── server/
│   └── LibraryServer.java # Server xử lý các yêu cầu
├── client/
│   ├── NetworkService.java # Service kết nối mạng
│   └── ui/
│       └── LibraryClientGUI.java # Giao diện người dùng
└── utils/
    └── BookDAO.java       # Data Access Object cho sách
libs/
└── gson-2.8.9.jar        # Thư viện JSON (cần tải về)
```

## Các chức năng
1. **Quản lý sách**: Thêm, sửa, xóa sách
2. **Tìm kiếm**: Tìm kiếm theo tên sách, tác giả, thể loại
3. **Mượn/Trả sách**: Quản lý việc mượn và trả sách
4. **Xem danh sách**: Hiển thị tất cả sách trong thư viện

## Hướng dẫn Setup

### 1. Tải thư viện Gson
- Truy cập: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.9/
- Tải file `gson-2.8.9.jar`
- Copy vào thư mục `libs/` của project

### 2. Cấu hình trong Eclipse
1. Right-click vào project → Properties
2. Java Build Path → Libraries
3. Add JARs → Chọn `libs/gson-2.8.9.jar`
4. Apply and Close

### 3. Chạy ứng dụng
1. **Chạy Server trước**:
   - Run `server/LibraryServer.java`
   - Server sẽ chạy trên port 12345

2. **Chạy Client**:
   - Run `client/ui/LibraryClientGUI.java`
   - Click "Connect" để kết nối đến server

## Giao thức TCP
- **Port**: 12345
- **Host**: localhost
- **Protocol**: TCP Socket
- **Data Format**: Java Object Serialization

## Lưu trữ dữ liệu
- File JSON: `books.json` (tự động tạo)
- Format: Array of Book objects

## Cấu trúc Request/Response
### Request Types:
- ADD_BOOK: Thêm sách mới
- UPDATE_BOOK: Cập nhật sách
- DELETE_BOOK: Xóa sách
- GET_ALL_BOOKS: Lấy tất cả sách
- SEARCH_BOOK: Tìm kiếm sách
- BORROW_BOOK: Mượn sách
- RETURN_BOOK: Trả sách

### Response Status:
- SUCCESS: Thành công
- ERROR: Lỗi
- NOT_FOUND: Không tìm thấy

## Các tính năng nâng cao có thể mở rộng
1. **Database**: Thay thế JSON bằng MySQL/PostgreSQL
2. **Authentication**: Đăng nhập người dùng
3. **Multi-threading**: Xử lý nhiều client đồng thời (đã implement cơ bản)
4. **Logging**: Ghi log hoạt động hệ thống
5. **Backup**: Sao lưu dữ liệu tự động
6. **Reports**: Báo cáo thống kê

## Lưu ý quan trọng
1. **Chạy Server trước Client**: Server phải chạy trước khi client kết nối
2. **Port conflicts**: Đảm bảo port 12345 không bị chiếm
3. **File permissions**: Đảm bảo có quyền ghi file `books.json`
4. **Java version**: Tương thích với Java 8+

## Troubleshooting
1. **Connection refused**: Kiểm tra server có đang chạy không
2. **ClassNotFoundException**: Kiểm tra classpath có Gson không
3. **Port in use**: Thay đổi port trong code nếu cần
4. **File not found**: Chương trình sẽ tự tạo file JSON nếu không tồn tại
