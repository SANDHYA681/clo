package controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.User;
import service.UserService;
import service.UserImageService;
import util.ValidationUtil;

/**
 * Servlet implementation class UserServlet
 */
// Servlet mapping defined in web.xml
@MultipartConfig
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService;
    private UserImageService userImageService;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserServlet() {
        super();
        userService = new UserService();
        userImageService = new UserImageService();
    }

    @Override
    public void init() throws ServletException {
        // Services are already initialized in the constructor
        super.init();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        switch (action) {
            case "profile":
                showProfile(request, response);
                break;
            case "dashboard":
                showDashboard(request, response);
                break;
            case "logout":
                logout(request, response);
                break;
            default:
                response.sendRedirect("index.jsp");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        switch (action) {
            case "login":
                login(request, response);
                break;
            case "register":
                register(request, response);
                break;
            case "updateProfile":
                updateProfile(request, response);
                break;
            case "updatePassword":
                changePassword(request, response);
                break;
            case "updateProfileImage":
                updateProfileImage(request, response);
                break;
            default:
                response.sendRedirect("index.jsp");
        }
    }

    /**
     * Show user dashboard
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        User user = userService.getUserById(userId);

        if (user == null) {
            session.invalidate();
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        request.setAttribute("user", user);
        // Redirect to the customer dashboard instead of using WEB-INF path
        response.sendRedirect("customer/dashboard.jsp");
    }

    /**
     * Show user profile
     */
    private void showProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        User user = userService.getUserById(userId);

        if (user == null) {
            session.invalidate();
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        // Get tab parameter
        String tab = request.getParameter("tab");
        if (tab == null) {
            tab = "profile";
        }

        request.setAttribute("user", user);
        request.setAttribute("tab", tab);

        // Redirect to the customer profile page instead of using WEB-INF path
        response.sendRedirect("customer/profile.jsp");
    }

    /**
     * User login
     */
    private void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "Email and password are required");
            request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);
            return;
        }

        User user = userService.authenticateUser(email, password);

        if (user == null) {
            request.setAttribute("errorMessage", "Invalid email or password");
            request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);
            return;
        }

        // Create session
        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userName", user.getFirstName());
        session.setAttribute("userRole", user.getRole());

        // Redirect based on role
        if (user.isAdmin()) {
            response.sendRedirect("admin/dashboard.jsp");
        } else {
            response.sendRedirect("HomeServlet");
        }
    }

    /**
     * User registration
     */
    private void register(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone = request.getParameter("phone");

        // Validate required fields
        if (firstName == null || lastName == null || email == null || password == null ||
            firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "All fields are required");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Validate name format (alphabetic characters only)
        if (!ValidationUtil.isValidName(firstName)) {
            request.setAttribute("errorMessage", "First name must contain only alphabetic characters");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        if (!ValidationUtil.isValidName(lastName)) {
            request.setAttribute("errorMessage", "Last name must contain only alphabetic characters");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Validate email format
        if (!ValidationUtil.isValidEmail(email)) {
            request.setAttribute("errorMessage", "Please enter a valid email address");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Validate password (at least 6 characters)
        if (!ValidationUtil.isValidPassword(password)) {
            request.setAttribute("errorMessage", "Password must be at least 6 characters long");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Validate phone number format (only digits allowed)
        if (!ValidationUtil.isValidPhone(phone)) {
            request.setAttribute("errorMessage", "Phone number must contain only digits");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Check if first name already exists (unique username)
        if (userService.firstNameExists(firstName)) {
            request.setAttribute("errorMessage", "Username already exists. Please choose a different first name");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Check if email already exists
        if (userService.emailExists(email)) {
            request.setAttribute("errorMessage", "Email already exists");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Check if phone number already exists (only if phone is provided)
        if (phone != null && !phone.isEmpty() && userService.phoneExists(phone)) {
            request.setAttribute("errorMessage", "Phone number already exists");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        // Register user
        User user = userService.registerUser(firstName, lastName, email, password, phone);

        if (user != null) {
            request.setAttribute("successMessage", "Registration successful. Please login.");
            request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);
        } else {
            request.setAttribute("errorMessage", "Registration failed. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
        }
    }

    /**
     * Update user profile
     */
    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        User user = userService.getUserById(userId);

        if (user == null) {
            session.invalidate();
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phone = request.getParameter("phone");
        String tab = request.getParameter("tab");

        // Validate input
        if (firstName == null || lastName == null || firstName.isEmpty() || lastName.isEmpty()) {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("First name and last name are required", "UTF-8"));
            return;
        }

        // Validate name format (alphabetic characters only)
        if (!ValidationUtil.isValidName(firstName)) {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("First name must contain only alphabetic characters", "UTF-8"));
            return;
        }

        if (!ValidationUtil.isValidName(lastName)) {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("Last name must contain only alphabetic characters", "UTF-8"));
            return;
        }

        // Validate phone number format (only digits allowed)
        if (!ValidationUtil.isValidPhone(phone)) {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("Phone number must contain only digits", "UTF-8"));
            return;
        }

        // Update user
        boolean success = userService.updateUserProfile(userId, firstName, lastName, phone);

        if (success) {
            // Update session
            user = userService.getUserById(userId); // Get updated user
            session.setAttribute("userName", user.getFirstName());
            session.setAttribute("user", user);

            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&success=" + java.net.URLEncoder.encode("Profile updated successfully", "UTF-8"));
        } else {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("Profile update failed. Please try again.", "UTF-8"));
        }
    }

    /**
     * Change user password
     */
    private void changePassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        User user = userService.getUserById(userId);

        if (user == null) {
            session.invalidate();
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        String tab = request.getParameter("tab");

        // Validate input
        if (currentPassword == null || newPassword == null || confirmPassword == null ||
            currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("All password fields are required", "UTF-8"));
            return;
        }

        // Validate new password length (at least 6 characters)
        if (!ValidationUtil.isValidPassword(newPassword)) {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("New password must be at least 6 characters long", "UTF-8"));
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("New passwords do not match", "UTF-8"));
            return;
        }

        // Update password
        boolean success = userService.updateUserPassword(userId, currentPassword, newPassword);

        if (success) {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&success=" + java.net.URLEncoder.encode("Password changed successfully", "UTF-8"));
        } else {
            response.sendRedirect("UserServlet?action=profile&tab=" + tab + "&error=" + java.net.URLEncoder.encode("Password change failed. Please try again.", "UTF-8"));
        }
    }

    /**
     * User logout
     */
    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("HomeServlet");
    }

    /**
     * Update profile image
     */
    private void updateProfileImage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        User user = userService.getUserById(userId);

        if (user == null) {
            session.invalidate();
            response.sendRedirect("LoginServlet?action=showLoginForm");
            return;
        }

        // Handle file upload
        Part filePart = request.getPart("profileImage");
        if (filePart != null && filePart.getSize() > 0) {
            // Upload the image using the service
            String webappRoot = request.getServletContext().getRealPath("/");
            String imageUrl = userImageService.uploadProfileImage(userId, filePart, webappRoot);

            if (imageUrl == null) {
                response.sendRedirect("UserServlet?action=profile&tab=profile-picture&error=" + java.net.URLEncoder.encode("Failed to upload image", "UTF-8"));
                return;
            }

            // Update user profile image in database
            boolean success = userImageService.updateUserProfileImage(userId, imageUrl);

            if (success) {
                // Update session
                user = userService.getUserById(userId); // Get updated user
                session.setAttribute("user", user);

                response.sendRedirect("UserServlet?action=profile&tab=profile-picture&success=" + java.net.URLEncoder.encode("Profile image updated successfully", "UTF-8"));
            } else {
                response.sendRedirect("UserServlet?action=profile&tab=profile-picture&error=" + java.net.URLEncoder.encode("Profile image update failed. Please try again.", "UTF-8"));
            }
        } else {
            response.sendRedirect("UserServlet?action=profile&tab=profile-picture&error=" + java.net.URLEncoder.encode("No image file selected", "UTF-8"));
        }
    }

    /**
     * Get the submitted file name from Part
     */
    private String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1);
            }
        }
        return null;
    }
}
