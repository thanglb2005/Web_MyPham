<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="jakarta.tags.core" %>
<!-- Force no favicon on JSP-admin pages -->
<link rel="icon" href="data:," />
<link rel="shortcut icon" href="data:," />
<link rel="apple-touch-icon" href="data:," />
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title><sitemesh:write property='title' /></title>

    <!-- CSS Libraries -->
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css"
    />
    <link
      rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
    />
    <link
      rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/4.1.1/animate.min.css"
    />

    <!-- Atlantis CSS -->
    <link rel="stylesheet" href="/assets/css/atlantis.min.css" />
    <link rel="stylesheet" href="/assets/css/atlantis.css" />
    <link rel="stylesheet" href="/assets/css/demo.css" />

    <!-- Custom CSS -->
    <link rel="stylesheet" href="/css/admin-fonts.css" />

    <!-- Page extra head from child -->
    <sitemesh:write property="head" />

    <!-- Admin Layout Styles -->
    <style>
      /* ===== HEADER STYLES ===== */
      .main-header {
        background: linear-gradient(135deg, #1e3c72, #2a5298);
        box-shadow: 0 4px 20px rgba(30, 60, 114, 0.3);
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 1000;
      }

      .main-header::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(
          45deg,
          rgba(255, 255, 255, 0.1) 0%,
          transparent 50%,
          rgba(255, 255, 255, 0.05) 100%
        );
        pointer-events: none;
      }

      .logo-header {
        display: flex;
        align-items: center;
        padding: 15px 25px;
        background: rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(10px);
        border-radius: 0 0 15px 0;
        margin-right: 20px;
      }

      .logo-header .logo {
        display: flex;
        align-items: center;
        text-decoration: none;
        color: white;
        font-weight: 700;
        font-size: 24px;
        transition: all 0.3s ease;
      }

      .logo-header .logo:hover {
        transform: scale(1.05);
        text-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
      }

      .logo-header .logo img {
        height: 45px;
        margin-right: 15px;
        filter: brightness(1.2);
        transition: all 0.3s ease;
      }

      .logo-header .logo:hover img {
        filter: brightness(1.5) drop-shadow(0 0 10px rgba(255, 255, 255, 0.3));
      }

      .toggle-sidebar {
        background: rgba(255, 255, 255, 0.2);
        border: 2px solid rgba(255, 255, 255, 0.3);
        color: white;
        border-radius: 8px;
        padding: 8px 12px;
        margin-left: 15px;
        transition: all 0.3s ease;
      }

      .toggle-sidebar:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(1.1);
      }

      .navbar-header {
        background: transparent;
        padding: 15px 25px;
      }

      .navbar-header h4 {
        color: white;
        margin: 0;
        font-weight: 600;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        font-size: 20px;
      }

      /* ===== SIDEBAR STYLES ===== */
      .sidebar {
        background: linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%);
        box-shadow: 4px 0 20px rgba(0, 0, 0, 0.1);
        border-right: 3px solid #007bff;
      }

      .sidebar-wrapper {
        padding: 0;
      }

      .sidebar-content {
        padding: 0;
      }

      .user-profile {
        background: linear-gradient(135deg, #007bff, #0056b3);
        padding: 20px;
        margin: 0;
        border-radius: 0 0 15px 15px;
        color: white;
        text-align: center;
        box-shadow: 0 4px 15px rgba(0, 123, 255, 0.3);
      }

      .user-profile img {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        border: 4px solid rgba(255, 255, 255, 0.3);
        margin-bottom: 15px;
        transition: all 0.3s ease;
      }

      .user-profile img:hover {
        transform: scale(1.1);
        border-color: rgba(255, 255, 255, 0.6);
      }

      .user-profile .user-name {
        font-weight: 600;
        font-size: 16px;
        margin-bottom: 5px;
      }

      .user-profile .user-role {
        font-size: 12px;
        opacity: 0.9;
        background: rgba(255, 255, 255, 0.2);
        padding: 4px 12px;
        border-radius: 15px;
        display: inline-block;
      }

      .nav-primary {
        padding: 20px 0;
      }

      .nav-primary .nav-item {
        margin: 5px 15px;
        border-radius: 10px;
        transition: all 0.3s ease;
      }

      .nav-primary .nav-item:hover {
        background: linear-gradient(135deg, #e3f2fd, #bbdefb);
        transform: translateX(5px);
      }

      .nav-primary .nav-item.active {
        background: linear-gradient(135deg, #007bff, #0056b3);
        box-shadow: 0 4px 15px rgba(0, 123, 255, 0.3);
      }

      .nav-primary .nav-item a {
        color: #495057;
        padding: 15px 20px;
        display: flex;
        align-items: center;
        text-decoration: none;
        font-weight: 500;
        border-radius: 10px;
        transition: all 0.3s ease;
      }

      .nav-primary .nav-item:hover a,
      .nav-primary .nav-item.active a {
        color: white;
        text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
      }

      .nav-primary .nav-item a i {
        margin-right: 12px;
        font-size: 18px;
        width: 20px;
        text-align: center;
      }

      .nav-primary .nav-item a p {
        margin: 0;
        font-size: 14px;
      }

      .nav-section {
        padding: 15px 20px 8px;
        color: #6c757d;
        font-size: 11px;
        font-weight: 700;
        letter-spacing: 1px;
        text-transform: uppercase;
        background: #f8f9fa;
        margin: 10px 0;
        border-left: 4px solid #007bff;
      }

      /* ===== MAIN CONTENT STYLES ===== */
      .main-panel {
        background: #f8f9fa;
        min-height: 100vh;
        margin-top: 60px; /* height of fixed header */
        margin-left: 250px; /* width of fixed sidebar */
      }

      .content {
        padding: 30px;
        background: transparent;
      }

      .page-header {
        background: linear-gradient(135deg, #ffffff, #f8f9fa);
        padding: 25px 30px;
        border-radius: 15px;
        margin-bottom: 25px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        border-left: 5px solid #007bff;
        position: relative;
        overflow: hidden;
      }

      .page-header::before {
        content: "";
        position: absolute;
        top: 0;
        right: 0;
        width: 100px;
        height: 100px;
        background: linear-gradient(45deg, rgba(0, 123, 255, 0.1), transparent);
        border-radius: 50%;
        transform: translate(30px, -30px);
      }

      .page-title {
        color: #1e3c72;
        font-weight: 700;
        font-size: 28px;
        margin: 0;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        position: relative;
        z-index: 1;
      }

      .page-inner {
        background: white;
        border-radius: 15px;
        padding: 30px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        border: 1px solid rgba(0, 123, 255, 0.1);
      }

      /* ===== MODAL STYLES ===== */
      .modal-content {
        border-radius: 15px;
        border: none;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
      }

      .modal-header {
        background: linear-gradient(135deg, #007bff, #0056b3);
        color: white;
        border-radius: 15px 15px 0 0;
        border: none;
        padding: 20px 25px;
      }

      .modal-header .close {
        color: white;
        opacity: 0.8;
        font-size: 28px;
      }

      .modal-header .close:hover {
        opacity: 1;
      }

      .modal-title {
        font-weight: 600;
      }

      .modal-body {
        padding: 25px;
      }

      .modal-footer {
        border-top: 1px solid #e9ecef;
        padding: 15px 25px;
      }

      .form-group label {
        font-weight: 600;
        margin-bottom: 8px;
        color: #495057;
      }

      .form-control:focus {
        border-color: #007bff;
        box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
      }

      .text-danger {
        color: #dc3545 !important;
      }

      /* ===== FOOTER STYLES ===== */
      .main-footer {
        background: linear-gradient(135deg, #1e3c72, #2a5298);
        color: white;
        padding: 25px 0;
        margin-top: 30px;
        position: relative;
        overflow: hidden;
        width: 100%;
        clear: both;
        display: block;
      }

      .main-footer::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 3px;
        background: linear-gradient(90deg, #007bff, #00d4ff, #007bff);
        animation: shimmer 3s infinite;
      }

      @keyframes shimmer {
        0% {
          transform: translateX(-100%);
        }
        100% {
          transform: translateX(100%);
        }
      }

      .footer-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0 30px;
      }

      .footer-left {
        display: flex;
        align-items: center;
        gap: 20px;
      }

      .footer-logo {
        display: flex;
        align-items: center;
        gap: 10px;
        font-weight: 600;
        font-size: 16px;
      }

      .footer-logo img {
        height: 30px;
        filter: brightness(1.2);
      }

      .footer-info {
        font-size: 14px;
        opacity: 0.9;
      }

      .footer-right {
        display: flex;
        align-items: center;
        gap: 15px;
      }

      .footer-links {
        display: flex;
        gap: 20px;
      }

      .footer-links a {
        color: white;
        text-decoration: none;
        font-size: 14px;
        opacity: 0.8;
        transition: all 0.3s ease;
      }

      .footer-links a:hover {
        opacity: 1;
        text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
      }

      .footer-copyright {
        text-align: center;
        padding-top: 15px;
        border-top: 1px solid rgba(255, 255, 255, 0.2);
        margin-top: 20px;
        font-size: 12px;
        opacity: 0.7;
      }

      /* ===== RESPONSIVE DESIGN ===== */
      @media (max-width: 768px) {
        .content {
          padding: 15px;
        }

        .page-header {
          padding: 20px;
          margin-bottom: 20px;
        }

        .page-title {
          font-size: 24px;
        }

        .page-inner {
          padding: 20px;
        }

        .footer-content {
          flex-direction: column;
          gap: 15px;
          text-align: center;
        }

        .footer-links {
          justify-content: center;
        }

        .logo-header {
          padding: 10px 15px;
        }

        .navbar-header {
          padding: 10px 15px;
        }

        .navbar-header h4 {
          font-size: 18px;
        }
      }

      /* ===== ANIMATIONS ===== */
      @keyframes fadeInUp {
        from {
          opacity: 0;
          transform: translateY(30px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      .page-header,
      .page-inner {
        animation: fadeInUp 0.6s ease-out;
      }

      /* ===== SCROLLBAR STYLING ===== */
      ::-webkit-scrollbar {
        width: 8px;
      }

      ::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 4px;
      }

      ::-webkit-scrollbar-thumb {
        background: linear-gradient(135deg, #007bff, #0056b3);
        border-radius: 4px;
      }

      ::-webkit-scrollbar-thumb:hover {
        background: linear-gradient(135deg, #0056b3, #004085);
      }

      /* ===== BUTTON STYLES ===== */
      .btn {
        border: none;
        border-radius: 8px;
        padding: 12px 20px;
        font-weight: 500;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        text-decoration: none;
        transition: all 0.3s ease;
        font-size: 14px;
      }

      .btn-primary {
        background: linear-gradient(135deg, #007bff, #0056b3);
        color: white;
        box-shadow: 0 4px 15px rgba(0, 123, 255, 0.3);
      }

      .btn-primary:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 123, 255, 0.4);
        background: linear-gradient(135deg, #0056b3, #004085);
      }

      .btn-info {
        background: linear-gradient(135deg, #17a2b8, #138496);
        color: white;
        box-shadow: 0 4px 15px rgba(23, 162, 184, 0.3);
      }

      .btn-info:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(23, 162, 184, 0.4);
      }

      .btn-danger {
        background: linear-gradient(135deg, #dc3545, #c82333);
        color: white;
        box-shadow: 0 4px 15px rgba(220, 53, 69, 0.3);
      }

      .btn-danger:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(220, 53, 69, 0.4);
      }

      /* ===== CARD STYLES ===== */
      .card {
        background: white;
        border-radius: 12px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        border: 1px solid rgba(0, 123, 255, 0.1);
        overflow: hidden;
        transition: all 0.3s ease;
      }

      .card:hover {
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }

      .card-header {
        background: linear-gradient(135deg, #f8f9fa, #e9ecef);
        border-bottom: 2px solid #007bff;
        padding: 20px;
        font-weight: 600;
        color: #495057;
        font-size: 16px;
      }

      .card-body {
        padding: 25px;
      }

      /* ===== TABLE STYLES ===== */
      .table {
        margin: 0;
        border-collapse: separate;
        border-spacing: 0;
      }

      .table th {
        background: linear-gradient(135deg, #007bff, #0056b3);
        color: white;
        border: none;
        padding: 15px;
        font-weight: 600;
        text-transform: uppercase;
        font-size: 12px;
        letter-spacing: 0.5px;
        position: sticky;
        top: 0;
        z-index: 10;
      }

      .table td {
        padding: 15px;
        border-bottom: 1px solid #f1f3f4;
        vertical-align: middle;
        transition: all 0.3s ease;
      }

      .table tbody tr:hover {
        background: #f8f9ff;
        transform: scale(1.01);
      }

      .table tbody tr:hover td {
        color: #007bff;
      }

      /* ===== FORM STYLES ===== */
      .form-control {
        border: 2px solid #e9ecef;
        border-radius: 8px;
        padding: 12px 15px;
        font-size: 14px;
        transition: all 0.3s ease;
      }

      .form-control:focus {
        border-color: #007bff;
        box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
        background: #fff;
      }

      .input-group-text {
        background: #f8f9fa;
        border: 2px solid #e9ecef;
        border-right: none;
        color: #6c757d;
      }

      .input-group .form-control {
        border-left: none;
      }

      .input-group .form-control:focus {
        border-left: none;
      }

      /* ===== BADGE STYLES ===== */
      .badge {
        padding: 8px 12px;
        border-radius: 20px;
        font-weight: 600;
        font-size: 12px;
      }

      .badge-primary {
        background: linear-gradient(135deg, #007bff, #0056b3);
        color: white;
      }

      /* ===== PAGINATION STYLES ===== */
      .pagination {
        justify-content: center;
        margin-top: 30px;
      }

      .page-link {
        color: #007bff;
        border: 1px solid #dee2e6;
        padding: 10px 15px;
        margin: 0 2px;
        border-radius: 8px;
        transition: all 0.3s ease;
        font-weight: 500;
      }

      .page-link:hover {
        background: #007bff;
        color: white;
        border-color: #007bff;
        transform: translateY(-2px);
      }

      .page-item.active .page-link {
        background: linear-gradient(135deg, #007bff, #0056b3);
        border-color: #007bff;
        color: white;
        box-shadow: 0 4px 15px rgba(0, 123, 255, 0.3);
      }

      /* ===== EMPTY STATE STYLES ===== */
      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #6c757d;
      }

      .empty-state i {
        font-size: 48px;
        margin-bottom: 20px;
        color: #dee2e6;
      }

      .empty-state h5 {
        margin-bottom: 10px;
        color: #495057;
        font-weight: 600;
      }

      .empty-state small {
        color: #6c757d;
      }

      .card-header {
        background: #f8f9fa;
        border-bottom: 1px solid #dee2e6;
        padding: 15px 20px;
        font-weight: 600;
        color: #495057;
      }

      .card-body {
        padding: 0;
      }

      .table {
        margin: 0;
        background: white;
      }

      .table thead th {
        background: #f8f9fa;
        border: none;
        padding: 10px 10px 10px 0;
        font-weight: 600;
        color: #495057;
        font-size: 0.9rem;
        text-align: center;
      }

      .table thead th:first-child {
        padding-left: 0;
      }

      .table tbody td {
        padding: 20px 10px 20px 0;
        border-bottom: none;
        vertical-align: middle;
        text-align: center;
      }

      .table tbody td:first-child {
        padding-left: 0;
      }

      .table tbody tr {
        border-bottom: 20px solid transparent;
      }

      .table tbody tr:hover {
        background: #f8f9fa;
      }

      .table tbody tr:last-child {
        border-bottom: none;
      }

      .category-image {
        max-width: 180px;
        max-height: 180px;
        object-fit: contain;
        border-radius: 8px;
        border: 2px solid #e9ecef;
        transition: all 0.3s ease;
        background: #f8f9fa;
        display: block;
        margin: 0 auto;
      }

      .category-image:hover {
        transform: scale(1.1);
        border-color: #007bff;
        box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
      }

      .no-image {
        width: 180px;
        height: 180px;
        background: #e9ecef;
        border-radius: 8px;
        border: 2px solid #e9ecef;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #6c757d;
        font-size: 36px;
        transition: all 0.3s ease;
        margin: 0 auto;
      }

      .no-image:hover {
        background: #dee2e6;
        border-color: #adb5bd;
      }

      .badge {
        padding: 4px 8px;
        border-radius: 15px;
        font-size: 0.75rem;
        font-weight: 600;
      }

      .badge-primary {
        background: #007bff;
        color: white;
      }

      .empty-state {
        text-align: center;
        padding: 40px 20px;
        color: #6c757d;
      }

      .empty-state i {
        font-size: 3rem;
        margin-bottom: 15px;
        opacity: 0.5;
      }

      .empty-state h3 {
        margin: 0 0 10px 0;
        color: #495057;
      }

      .empty-state p {
        margin: 0;
      }

      .btn-edit {
        background: linear-gradient(135deg, #ffc107 0%, #ffb300 100%);
        color: white;
        border: none;
        border-radius: 8px;
        padding: 8px 12px;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 2px 4px rgba(255, 193, 7, 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        min-width: 40px;
        height: 36px;
      }

      .btn-edit:hover {
        background: linear-gradient(135deg, #ffb300 0%, #ff8f00 100%);
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(255, 193, 7, 0.4);
      }

      .btn-delete {
        background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
        color: white;
        border: none;
        border-radius: 8px;
        padding: 8px 12px;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 2px 4px rgba(220, 53, 69, 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        min-width: 40px;
        height: 36px;
      }

      .btn-delete:hover {
        background: linear-gradient(135deg, #c82333 0%, #bd2130 100%);
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(220, 53, 69, 0.4);
      }

      .btn-edit i,
      .btn-delete i {
        font-size: 14px;
      }

      .action-buttons {
        display: flex;
        gap: 8px;
        justify-content: center;
        align-items: center;
      }
    </style>
  </head>
  <body>
    <div class="wrapper">
      <!-- Header -->
      <div class="main-header">
        <div class="logo-header">
          <a href="/admin/home" class="logo">
            <img src="/images/logo/logo.png" width="180" alt="OneShop" />
          </a>
          <button
            class="btn btn-link btn-sm toggle-sidebar"
            onclick="toggleSidebar()"
          >
            <i class="icon-menu"></i>
          </button>
        </div>
        <nav class="navbar navbar-header navbar-expand-lg">
          <div class="container-fluid">
            <h4 style="color: white; margin: 0">OneShop Admin</h4>
          </div>
        </nav>
      </div>

      <!-- Sidebar -->
      <div
        class="sidebar sidebar-style-2"
        style="
          position: fixed;
          top: 60px;
          left: 0;
          bottom: 0;
          z-index: 1028;
          width: 250px;
          background: #f8f9fa;
          border-right: 1px solid #e9ecef;
          margin: 0;
          padding: 0;
        "
      >
        <style>
          .nav-item .collapse {
            display: none;
          }
          .nav-item .collapse.show {
            display: block;
          }
          .nav-item.active > a {
            color: #fff !important;
          }
          .nav-item .caret {
            transition: transform 0.3s ease;
          }
          .nav-item.active .caret {
            transform: rotate(90deg);
          }

          /* Simple styling - no hover animations, black text */
          .nav-item a {
            color: #000 !important;
            text-decoration: none;
          }
          .nav-item a:hover {
            color: #000 !important;
            background-color: transparent !important;
          }
          .nav-item.active a {
            color: #fff !important;
          }
          .sub-item {
            color: #000 !important;
          }
          .nav-section {
            color: #000 !important;
          }

          /* Remove all background colors from sub-menu items */
          .nav-collapse .nav-item a {
            background-color: transparent !important;
            color: #000 !important;
          }
          .nav-collapse .nav-item a:hover {
            background-color: transparent !important;
            color: #000 !important;
          }
          .nav-collapse .nav-item.active a {
            background-color: transparent !important;
            color: #000 !important;
          }
        </style>
        <div class="sidebar-wrapper scrollbar scrollbar-inner">
          <div class="sidebar-content">
            <!-- User Profile -->
            <div class="user">
              <div class="avatar-sm float-left mr-2">
                <img
                  src="/assets/img/profile.jpg"
                  alt="..."
                  class="avatar-img rounded-circle"
                />
              </div>
              <div class="info">
                <a
                  data-toggle="collapse"
                  href="#collapseExample"
                  aria-expanded="true"
                >
                  <span>
                    Tên: ${sessionScope.user != null ? sessionScope.user.name :
                    'Admin OneShop'}
                    <span class="user-level">Administrator</span>
                  </span>
                </a>
                <div class="clearfix"></div>
              </div>
            </div>

            <!-- Navigation Menu -->
            <ul class="nav nav-primary">
              <li class="nav-item active">
                <a
                  data-toggle="collapse"
                  href="#dashboard"
                  class="collapsed"
                  aria-expanded="false"
                  data-tooltip="Trang chủ"
                >
                  <i class="fas fa-home"></i>
                  <p>Trang chủ</p>
                  <span class="caret"></span>
                </a>
                <div class="collapse" id="dashboard">
                  <ul class="nav nav-collapse">
                    <li>
                      <a href="/admin/home">
                        <span class="sub-item">Trang chủ</span>
                      </a>
                    </li>
                  </ul>
                </div>
              </li>

              <li class="nav-section">
                <span class="sidebar-mini-icon">
                  <i class="fa fa-ellipsis-h"></i>
                </span>
                <h4 class="text-section">Các thành phần</h4>
              </li>

              <li class="nav-item">
                <a href="/admin/shops" data-tooltip="Duyệt đăng ký shop">
                  <i class="fas fa-store"></i>
                  <p>Duyệt Shop</p>
                </a>
              </li>

              <li class="nav-item">
                <a
                  data-toggle="collapse"
                  href="#tables"
                  data-tooltip="Hệ thống quản lý"
                >
                  <i class="fas fa-table"></i>
                  <p>Hệ thống quản lý</p>
                  <span class="caret"></span>
                </a>
                <div class="collapse" id="tables">
                  <ul class="nav nav-collapse">
                    <li>
                      <a href="/admin/categories">
                        <span class="sub-item">Quản lý Thể loại</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/products">
                        <span class="sub-item">Quản lý Sản phẩm</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/shops">
                        <span class="sub-item">Quản lý Shop</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/brands">
                        <span class="sub-item">Quản lý Thương hiệu</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/providers">
                        <span class="sub-item">Quản lý nhà vận chuyển</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/shippers-list">
                        <span class="sub-item">Quản lý Shipper</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/orders">
                        <span class="sub-item">Quản lý Đơn hàng</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/users">
                        <span class="sub-item">Quản lý Khách hàng</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/promotions">
                        <span class="sub-item">Quản Lý Khuyến Mãi</span>
                      </a>
                    </li>
                  </ul>
                </div>
              </li>

              <li class="nav-item">
                <a
                  data-toggle="collapse"
                  href="#charts"
                  data-tooltip="Thống kê doanh số"
                >
                  <i class="far fa-chart-bar"></i>
                  <p>Thống kê doanh số</p>
                  <span class="caret"></span>
                </a>
                <div class="collapse" id="charts">
                  <ul class="nav nav-collapse">
                    <li>
                      <a href="/admin/statistics#products">
                        <span class="sub-item">Thống kê sản phẩm</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/category-statistics">
                        <span class="sub-item">Thống kê danh mục</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/customer-statistics">
                        <span class="sub-item">Thống kê khách hàng</span>
                      </a>
                    </li>
                    <li>
                      <a href="/admin/revenue-statistics">
                        <span class="sub-item">Thống kê doanh thu</span>
                      </a>
                    </li>
                  </ul>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Main Panel -->
      <div class="main-panel">
        <div class="content">
          <sitemesh:write property="body" />
        </div>

        <!-- Footer -->
        <footer class="main-footer">
          <div class="footer-content">
            <div class="footer-left">
              <div class="footer-logo">
                <img src="/images/logo/logo.png" alt="OneShop" />
                <span>OneShop Admin</span>
              </div>
              <div class="footer-info">Hệ thống quản lý cửa hàng mỹ phẩm</div>
            </div>
            <div class="footer-right">
              <div class="footer-links">
                <a href="/admin/home">Trang chủ</a>
                <a href="/admin/categories">Danh mục</a>
                <a href="/admin/products">Sản phẩm</a>
                <a href="/admin/orders">Đơn hàng</a>
              </div>
            </div>
          </div>
          <div class="footer-copyright">
            © 2025 OneShop Admin Panel. Được kế bởi OneShop Team - HCMUTE.
          </div>
        </footer>
      </div>
    </div>

    <!-- JavaScript Libraries -->
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <!-- Atlantis Core Scripts -->
    <script src="/assets/js/atlantis.min.js"></script>

    <!-- Additional Plugins -->
    <script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>
    <script src="/assets/js/plugin/jquery-ui-touch-punch/jquery.ui.touch-punch.min.js"></script>
    <script src="/assets/js/plugin/jquery-scrollbar/jquery.scrollbar.min.js"></script>

    <script>
      function toggleSidebar() {
        const wrapper = document.querySelector(".wrapper");
        const minibutton = document.querySelector(".toggle-sidebar");
        if (wrapper.classList.contains("sidebar_minimize")) {
          wrapper.classList.remove("sidebar_minimize");
          minibutton.classList.remove("toggled");
          minibutton.innerHTML = '<i class="icon-menu"></i>';
        } else {
          wrapper.classList.add("sidebar_minimize");
          minibutton.classList.add("toggled");
          minibutton.innerHTML = '<i class="icon-options-vertical"></i>';
        }
        window.dispatchEvent(new Event("resize"));
      }

      // Bootstrap collapse functionality for sidebar menu
      $(document).ready(function () {
        console.log("Admin decorator loaded");

        // Delegate collapse only for sidebar and only when href is an id
        $(".sidebar").on("click", '[data-toggle="collapse"]', function (e) {
          const href = $(this).attr("href");
          if (href && href.startsWith("#")) {
            e.preventDefault();
            $(href).collapse("toggle");
          }
        });

        // Add active class for clicked menu item
        $('.nav-item a[data-toggle="collapse"]').on("click", function () {
          $(".nav-item").removeClass("active");
          $(this).closest(".nav-item").addClass("active");
        });

        // Ensure menu opens correctly when page loads
        $(".collapse").on("show.bs.collapse", function () {
          $(this).siblings(".nav-item").addClass("active");
        });

        $(".collapse").on("hide.bs.collapse", function () {
          $(this).siblings(".nav-item").removeClass("active");
        });
      });
    </script>
  </body>
</html>
