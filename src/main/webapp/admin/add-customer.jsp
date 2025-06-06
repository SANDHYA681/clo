<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>

<%
// Check if user is logged in and is an admin
Object userObj = session.getAttribute("user");
if (userObj == null) {
    response.sendRedirect(request.getContextPath() + "/LoginServlet");
    return;
}

User currentUser = (User) userObj;
if (!currentUser.isAdmin()) {
    response.sendRedirect(request.getContextPath() + "/LoginServlet");
    return;
}

// Get form values if they were set (in case of validation error)
String firstName = (String) request.getAttribute("firstName");
String lastName = (String) request.getAttribute("lastName");
String email = (String) request.getAttribute("email");
String phone = (String) request.getAttribute("phone");
String error = (String) request.getAttribute("error");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add New Customer - Admin Dashboard</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/admin-dashboard.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/admin-blue-theme-all.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/admin-customers-enhanced.css">

</head>
<body>
    <div class="dashboard-container">
        <div class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <a href="<%= request.getContextPath() %>/index.jsp" class="logo">
                    <i class="fas fa-tshirt"></i>
                    <span>CLOTHEE</span>
                </a>
                <div class="user-info">
                    <div class="user-avatar">
                        <i class="fas fa-user-shield"></i>
                    </div>
                    <h3 class="user-name"><%= currentUser.getFirstName() %> <%= currentUser.getLastName() %></h3>
                    <p class="user-role">Admin</p>
                </div>
            </div>

            <div class="sidebar-menu">
                <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}dashboard.jsp" class="menu-item">
                    <i class="fas fa-tachometer-alt menu-icon"></i>
                    Dashboard
                </a>
                <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}products.jsp" class="menu-item">
                    <i class="fas fa-box menu-icon"></i>
                    Products
                </a>
                <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}categories.jsp" class="menu-item">
                    <i class="fas fa-tags menu-icon"></i>
                    Categories
                </a>
                <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}orders.jsp" class="menu-item">
                    <i class="fas fa-shopping-bag menu-icon"></i>
                    Orders
                </a>
                <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}customers.jsp" class="menu-item active">
                    <i class="fas fa-users menu-icon"></i>
                    Customers
                </a>
                <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}reviews.jsp" class="menu-item">
                    <i class="fas fa-star menu-icon"></i>
                    Reviews
                </a>
                <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}messages.jsp" class="menu-item">
                    <i class="fas fa-envelope menu-icon"></i>
                    Messages
                </a>
                <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}settings.jsp" class="menu-item">
                    <i class="fas fa-cog menu-icon"></i>
                    Settings
                </a>
                <a href="<%= request.getContextPath() %>/LogoutServlet" class="menu-item">
                    <i class="fas fa-sign-out-alt menu-icon"></i>
                    Logout
                </a>
            </div>
        </div>

        <div class="main-content">
            <div class="dashboard-header">
                <h1 class="page-title">Add New Customer</h1>
                <div class="header-actions">
                    <a href="../index.jsp" class="header-action" title="View Store">
                        <i class="fas fa-store"></i>
                    </a>
                </div>
            </div>

            <a href="${baseUrl != null ? baseUrl : request.getContextPath().concat('/admin/')}customers.jsp" class="back-link">
                <i class="fas fa-arrow-left"></i> Back to Customers
            </a>

        <% if (error != null) { %>
        <div class="alert alert-danger">
            <%= error %>
        </div>
        <% } %>

        <div class="form-container">
            <form action="<%= request.getContextPath() %>/AdminUserServlet" method="post">
                <input type="hidden" name="action" value="add">

                <div class="form-group">
                    <label for="firstName" class="form-label required-field">First Name</label>
                    <input type="text" id="firstName" name="first_name" class="form-control" value="<%= firstName != null ? firstName : "" %>" required>
                </div>

                <div class="form-group">
                    <label for="lastName" class="form-label required-field">Last Name</label>
                    <input type="text" id="lastName" name="last_name" class="form-control" value="<%= lastName != null ? lastName : "" %>" required>
                </div>

                <div class="form-group">
                    <label for="email" class="form-label required-field">Email Address</label>
                    <input type="email" id="email" name="email" class="form-control" value="<%= email != null ? email : "" %>" required>
                </div>

                <div class="form-group">
                    <label for="password" class="form-label required-field">Password</label>
                    <input type="password" id="password" name="password" class="form-control" required>
                    <span class="form-hint">Password must be at least 8 characters long.</span>
                </div>

                <div class="form-group">
                    <label for="phone" class="form-label">Phone Number</label>
                    <input type="tel" id="phone" name="phone" class="form-control" value="<%= phone != null ? phone : "" %>">
                </div>

                <div class="form-group">
                    <label for="role" class="form-label required-field">Role</label>
                    <select id="role" name="role" class="form-control" required>
                        <option value="user" selected>Customer</option>
                    </select>
                    <small class="form-text text-muted">Only customer role is available for new users.</small>
                </div>

                <!-- Profile image upload is disabled for now -->
                <input type="hidden" name="profileImage" value="default.jpg">

                <div class="form-actions">
                    <a href="customers.jsp" class="btn btn-secondary">Cancel</a>
                    <button type="submit" class="btn btn-primary">Add Customer</button>
                </div>
            </form>
        </div>
        </div>
    </div>

    <script>
        // Toggle sidebar on mobile
        const toggleSidebar = document.getElementById('toggleSidebar');
        const sidebar = document.getElementById('sidebar');

        if (toggleSidebar) {
            toggleSidebar.addEventListener('click', () => {
                sidebar.classList.toggle('active');
            });
        }
    </script>
</body>
</html>
