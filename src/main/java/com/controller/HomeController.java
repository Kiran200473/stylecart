package com.stylecart.controller;

import jakarta.servlet.http.HttpSession;
import com.stylecart.model.OrderHistory;
import com.stylecart.repository.OrderHistoryRepository;
import com.stylecart.model.Wishlist;
import com.stylecart.repository.WishlistRepository;
import com.stylecart.model.CartItem;
import com.stylecart.model.Product;
import com.stylecart.model.User;
import com.stylecart.repository.CartItemRepository;
import com.stylecart.repository.ProductRepository;
import com.stylecart.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

@Autowired
private CartItemRepository cartItemRepository;

@Autowired
private ProductRepository productRepository;

@Autowired
private UserRepository userRepository;

@Autowired
private WishlistRepository wishlistRepository;

@Autowired
private OrderHistoryRepository orderHistoryRepository;

private boolean isAdmin(HttpSession session) {

        Boolean admin =
                (Boolean) session.getAttribute("admin");

        return admin != null && admin;
    }

@GetMapping("/")
public String loginPage() {
    return "login";
}

@PostMapping("/login")
public String loginUser(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpSession session) {

    if(email.equals("admin@stylecart.com")
            && password.equals("admin123")) {

        session.setAttribute("admin", true);

        return "redirect:/admin";
    }

    User user = userRepository.findByEmail(email);

    if (user != null &&
            user.getPassword().equals(password)) {

        session.setAttribute("admin", false);
session.setAttribute("userId", user.getId());
session.setAttribute("customerName", user.getName());
session.setAttribute("userEmail", user.getEmail());

return "redirect:/home";
    }

    return "redirect:/";
}

@GetMapping("/logout")
public String logout(HttpSession session) {

    session.invalidate();

    return "redirect:/";
}

@GetMapping("/home")
public String homePage() {
    return "index";
}

@GetMapping("/products")
public String productsPage(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        Model model) {

    List<Product> products;

    if (keyword != null && !keyword.isEmpty()) {

        products =
                productRepository
                        .findByNameContainingIgnoreCase(keyword);

    }
    else if (category != null &&
             !category.isEmpty()) {

        products =
                productRepository
                        .findByCategory(category);

    }
    else {

        products =
                productRepository.findAll();
    }

    model.addAttribute("products", products);
    model.addAttribute("keyword", keyword);
    model.addAttribute("category", category);

    return "products";
}

@GetMapping("/product/{id}")
public String productDetails(@PathVariable Long id,
                             Model model) {

    Product product =
            productRepository.findById(id).orElse(null);

    model.addAttribute("product", product);

    return "product-details";
}

@GetMapping("/add-cart/{id}")
public String addToCart(@PathVariable Long id,
                        HttpSession session) {

    Long userId =
            (Long) session.getAttribute("userId");

    String customerName =
            (String) session.getAttribute("customerName");

    Product product =
            productRepository.findById(id).orElse(null);

    if (product != null) {

        List<CartItem> cartItems =
                cartItemRepository.findByUserId(userId);

        CartItem existingItem = null;

        for (CartItem item : cartItems) {

            if (item.getProductName()
                    .equals(product.getName())) {

                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {

            existingItem.setQuantity(
                    existingItem.getQuantity() + 1);

            cartItemRepository.save(existingItem);

        } else {

            CartItem item = new CartItem();

            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setImageUrl(product.getImageUrl());
            item.setQuantity(1);

            item.setUserId(userId);
            item.setCustomerName(customerName);

            cartItemRepository.save(item);
        }
    }

    return "redirect:/cart";
}

@GetMapping("/add-wishlist/{id}")
public String addToWishlist(@PathVariable Long id) {

    Product product =
            productRepository.findById(id).orElse(null);

    if(product != null) {

        Wishlist wishlist = new Wishlist();

        wishlist.setProductName(product.getName());
        wishlist.setPrice(product.getPrice());
        wishlist.setImageUrl(product.getImageUrl());

        wishlistRepository.save(wishlist);
    }

    return "redirect:/wishlist";
}

@GetMapping("/wishlist")
public String wishlistPage(Model model) {

    model.addAttribute(
            "wishlistItems",
            wishlistRepository.findAll()
    );

    return "wishlist";
}

@GetMapping("/remove-wishlist/{id}")
public String removeWishlist(@PathVariable Long id) {

    wishlistRepository.deleteById(id);

    return "redirect:/wishlist";
}

@GetMapping("/wishlist-to-cart/{id}")
public String wishlistToCart(@PathVariable Long id) {

    Wishlist item =
            wishlistRepository.findById(id).orElse(null);

    if(item != null) {

        CartItem cart = new CartItem();

        cart.setProductName(item.getProductName());
        cart.setPrice(item.getPrice());
        cart.setImageUrl(item.getImageUrl());
        cart.setQuantity(1);

        cartItemRepository.save(cart);

        wishlistRepository.deleteById(id);
    }

    return "redirect:/wishlist";
}

@GetMapping("/cart")
public String cartPage(Model model,
                       HttpSession session) {

    Long userId =
            (Long) session.getAttribute("userId");

    List<CartItem> cartItems =
            cartItemRepository.findByUserId(userId);

    double total =
            cartItems.stream()
                    .mapToDouble(item ->
                            item.getPrice() * item.getQuantity())
                    .sum();

    model.addAttribute("cartItems", cartItems);
    model.addAttribute("total", total);

    return "cart";
}

@GetMapping("/remove-cart/{id}")
public String removeCartItem(@PathVariable Long id) {

    cartItemRepository.deleteById(id);

    return "redirect:/cart";
}

@GetMapping("/increase-quantity/{id}")
public String increaseQuantity(@PathVariable Long id) {

    CartItem item =
            cartItemRepository.findById(id).orElse(null);

    if(item != null) {

        item.setQuantity(item.getQuantity() + 1);

        cartItemRepository.save(item);
    }

    return "redirect:/cart";
}

@GetMapping("/decrease-quantity/{id}")
public String decreaseQuantity(@PathVariable Long id) {

    CartItem item =
            cartItemRepository.findById(id).orElse(null);

    if(item != null) {

        if(item.getQuantity() > 1) {

            item.setQuantity(item.getQuantity() - 1);

            cartItemRepository.save(item);

        } else {

            cartItemRepository.deleteById(id);
        }
    }

    return "redirect:/cart";
}

@GetMapping("/checkout")
public String checkoutPage(Model model,
                           HttpSession session) {

    Long userId =
            (Long) session.getAttribute("userId");

    List<CartItem> cartItems =
            cartItemRepository.findByUserId(userId);

    double total =
            cartItems.stream()
                    .mapToDouble(item ->
                            item.getPrice() * item.getQuantity())
                    .sum();

    model.addAttribute("total", total);

    return "checkout";
}

@GetMapping("/success")
public String successPage(HttpSession session) {

    Long userId =
            (Long) session.getAttribute("userId");

    String customerName =
            (String) session.getAttribute("customerName");

    List<CartItem> cartItems =
            cartItemRepository.findByUserId(userId);

    for (CartItem item : cartItems) {

        OrderHistory order =
                new OrderHistory();

        order.setProductName(item.getProductName());
        order.setImageUrl(item.getImageUrl());
        order.setPrice(item.getPrice());
        order.setQuantity(item.getQuantity());

        order.setUserId(userId);
        order.setCustomerName(customerName);

        orderHistoryRepository.save(order);
    }

    for (CartItem item : cartItems) {
        cartItemRepository.delete(item);
    }

    return "success";
}

@GetMapping("/order-history")
public String orderHistoryPage(Model model,
                               HttpSession session) {

    Long userId =
            (Long) session.getAttribute("userId");

    model.addAttribute(
            "orders",
            orderHistoryRepository.findByUserId(userId));

    return "order-history";
}

@GetMapping("/register")
public String registerPage() {
    return "register";
}

@GetMapping("/admin")
public String adminPage(HttpSession session) {

    Boolean admin =
            (Boolean) session.getAttribute("admin");

    if(admin == null || !admin) {
        return "redirect:/home";
    }

    return "admin";
}

@GetMapping("/admin/products")
public String adminProducts(Model model,
                            HttpSession session) {

    if (!isAdmin(session)) {
        return "redirect:/home";
    }

    model.addAttribute(
            "products",
            productRepository.findAll()
    );

    return "admin-products";
}

@GetMapping("/add-product")
public String addProductPage(HttpSession session) {

    if (!isAdmin(session)) {
        return "redirect:/home";
    }

    return "add-product";
}

@PostMapping("/register")
public String saveUser(User user) {

    userRepository.save(user);

    return "redirect:/";
}

@PostMapping("/save-product")
public String saveProduct(Product product) {

    productRepository.save(product);

    return "redirect:/products";
}

@GetMapping("/edit-product/{id}")
public String editProduct(@PathVariable Long id,
                          Model model,
                          HttpSession session) {

    if (!isAdmin(session)) {
        return "redirect:/home";
    }

    Product product =
            productRepository.findById(id).orElse(null);

    model.addAttribute("product", product);

    return "edit-product";
}

@PostMapping("/update-product")
public String updateProduct(Product product,
                            HttpSession session) {

    if (!isAdmin(session)) {
        return "redirect:/home";
    }

    productRepository.save(product);

    return "redirect:/admin/products";
}

@GetMapping("/delete-product/{id}")
public String deleteProduct(@PathVariable Long id,
                            HttpSession session) {

    if (!isAdmin(session)) {
        return "redirect:/home";
    }

    productRepository.deleteById(id);

    return "redirect:/products";
}

@GetMapping("/load-products")
public String loadProducts() {

    productRepository.deleteAll(); 

        Product p1 = new Product();
        p1.setName("Shirt");
        p1.setPrice(4999);
        p1.setCategory("Women Fashion");
        p1.setRating(6.5);
        p1.setDiscount(15);
        p1.setDescription("Comfortable and 100% cotton butter yellow stylish shirt for daily wear.");
        p1.setImageUrl("/images/wshirt1.jpg");

        Product p2 = new Product();
        p2.setName("Stylish Crop Top");
        p2.setPrice(1999);
        p2.setCategory("Women Fashion");
        p2.setRating(7.8);
        p2.setDiscount(10);
        p2.setDescription("Comfortable and stylish summer crop top for daily wear.");
        p2.setImageUrl("/images/stylishtop.jpg");

        Product p3 = new Product();
        p3.setName("Stylish Gown");
        p3.setPrice(7999);
        p3.setCategory("Women Fashion");
        p3.setRating(9.8);
        p3.setDiscount(20);
        p3.setDescription("Comfortable ladys gown for party wear.");
        p3.setImageUrl("/images/gown.jpg");

        Product p4 = new Product();
        p4.setName("Ladys Shoes");
        p4.setPrice(7999);
        p4.setCategory("Shoes");
        p4.setRating(4.8);
        p4.setDiscount(18);
        p4.setDescription("Comfortable ladys shoes for daily wear.");
        p4.setImageUrl("/images/wshoes.jpg");

        Product p5 = new Product();
        p5.setName("Jeans");
        p5.setPrice(2999);
        p5.setCategory("Women Fashion");
        p5.setRating(5.9);
        p5.setDiscount(14);
        p5.setDescription("Comfortable ladys jean for daily wear.");
        p5.setImageUrl("/images/wjeans.jpg");
       
        Product p6 = new Product();
        p6.setName("Premium T-Shirt");
        p6.setPrice(999);
        p6.setCategory("Fashion");
        p6.setRating(4.8);
        p6.setDiscount(20);
        p6.setDescription("Premium cotton t-shirt for daily wear.");
        p6.setImageUrl("/images/tshirt.jpg");

        Product p7 = new Product();
        p7.setName("Oversized Hoodie");
        p7.setPrice(1499);
        p7.setCategory("Fashion");
        p7.setRating(4.7);
        p7.setDiscount(15);
        p7.setDescription("Comfortable oversized hoodie.");
        p7.setImageUrl("/images/hoodie.jpg");

        Product p8 = new Product();
        p8.setName("Denim Jean's");
        p8.setPrice(2299);
        p8.setCategory("Fashion");
        p8.setRating(4.9);
        p8.setDiscount(18);
        p8.setDescription("Stylish denim jacket.");
        p8.setImageUrl("https://images.unsplash.com/photo-1541099649105-f69ad21f3246");

        Product p9 = new Product();
        p9.setName("Slim Fit Jeans");
        p9.setPrice(1899);
        p9.setCategory("Fashion");
        p9.setRating(4.6);
        p9.setDiscount(12);
        p9.setDescription("Modern slim fit jeans.");
        p9.setImageUrl("https://images.unsplash.com/photo-1542272604-787c3835535d");

        Product p10 = new Product();
        p10.setName("Running Shoes");
        p10.setPrice(2499);
        p10.setCategory("Shoes");
        p10.setRating(4.7);
        p10.setDiscount(15);
        p10.setDescription("Comfortable running shoes.");
        p10.setImageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff");

        Product p11 = new Product();
        p11.setName("Sneakers");
        p11.setPrice(2999);
        p11.setCategory("Shoes");
        p11.setRating(4.8);
        p11.setDiscount(20);
        p11.setDescription("Classic white sneakers.");
        p11.setImageUrl("https://images.unsplash.com/photo-1549298916-b41d501d3772");

        Product p12 = new Product();
        p12.setName("Sports Shoes");
        p12.setPrice(3299);
        p12.setCategory("Shoes");
        p12.setRating(4.7);
        p12.setDiscount(18);
        p12.setDescription("High-performance sports shoes.");
        p12.setImageUrl("https://images.unsplash.com/photo-1460353581641-37baddab0fa2");

        Product p13 = new Product();
        p13.setName("Smart Watch");
        p13.setPrice(3999);
        p13.setCategory("Watches");
        p13.setRating(4.9);
        p13.setDiscount(25);
        p13.setDescription("Fitness tracking smartwatch.");
        p13.setImageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30");

        Product p14 = new Product();
        p14.setName("Luxury Watch");
        p14.setPrice(8999);
        p14.setCategory("Watches");
        p14.setRating(4.9);
        p14.setDiscount(10);
        p14.setDescription("Premium luxury wrist watch.");
        p14.setImageUrl("https://images.unsplash.com/photo-1524592094714-0f0654e20314");

        Product p15 = new Product();
        p15.setName("Luxury Women's Watch");
        p15.setPrice(9999);
        p15.setCategory("Watches");
        p15.setRating(5.9);
        p15.setDiscount(19);
        p15.setDescription("Elegant Design: Stylish square case with a stone-studded bracelet strap watch.");
        p15.setImageUrl("/images/watch.jpg");

        Product p16 = new Product();
        p16.setName("iPhone");
        p16.setPrice(79999);
        p16.setCategory("Electronics");
        p16.setRating(5.0);
        p16.setDiscount(10);
        p16.setDescription("Premium smartphone.");
        p16.setImageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9");

        Product p17 = new Product();
        p17.setName("Samsung Galaxy");
        p17.setPrice(69999);
        p17.setCategory("Electronics");
        p17.setRating(4.8);
        p17.setDiscount(12);
        p17.setDescription("Flagship Android smartphone.");
        p17.setImageUrl("https://images.unsplash.com/photo-1610945265064-0e34e5519bbf");

        Product p18 = new Product();
        p18.setName("Laptop");
        p18.setPrice(59999);
        p18.setCategory("Electronics");
        p18.setRating(4.9);
        p18.setDiscount(8);
        p18.setDescription("Powerful laptop for work.");
        p18.setImageUrl("https://images.unsplash.com/photo-1496181133206-80ce9b88a853");

        Product p19 = new Product();
        p19.setName("Wireless Earbuds");
        p19.setPrice(2499);
        p19.setCategory("Electronics");
        p19.setRating(4.7);
        p19.setDiscount(15);
        p19.setDescription("Noise cancelling earbuds.");
        p19.setImageUrl("https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46");

        Product p20 = new Product();
        p20.setName("Perfume");
        p20.setPrice(1499);
        p20.setCategory("Beauty");
        p20.setRating(4.8);
        p20.setDiscount(20);
        p20.setDescription("Long-lasting fragrance.");
        p20.setImageUrl("https://images.unsplash.com/photo-1592945403244-b3fbafd7f539");

        Product p21 = new Product();
        p21.setName("Face Wash");
        p21.setPrice(399);
        p21.setCategory("Beauty");
        p21.setRating(4.6);
        p21.setDiscount(10);
        p21.setDescription("Refreshing daily face wash.");
        p21.setImageUrl("https://images.unsplash.com/photo-1556228578-8c89e6adf883");

        Product p22 = new Product();
        p22.setName("Beauty Products");
        p22.setPrice(33999);
        p22.setCategory("Beauty");
        p22.setRating(6.6);
        p22.setDiscount(17);
        p22.setDescription("use products dedicated to hydrating, protecting, and repairing the skin.");
        p22.setImageUrl("/images/beauty.jpg");

        Product p23 = new Product();
        p23.setName("Table Lamp");
        p23.setPrice(1299);
        p23.setCategory("Home Decor");
        p23.setRating(4.7);
        p23.setDiscount(15);
        p23.setDescription("Modern decorative lamp.");
        p23.setImageUrl("https://images.unsplash.com/photo-1507473885765-e6ed057f782c");

        Product p24 = new Product();
        p24.setName("Modern Chair");
        p24.setPrice(3999);
        p24.setCategory("Home Decor");
        p24.setRating(4.8);
        p24.setDiscount(18);
        p24.setDescription("Comfortable modern chair.");
        p24.setImageUrl("/images/chair.jpg");

        Product p25 = new Product();
p25.setName("Women's Crop Top");
p25.setPrice(899);
p25.setCategory("Women Fashion");
p25.setRating(4.8);
p25.setDiscount(20);
p25.setDescription("Stylish women's crop top for casual wear.");
p25.setImageUrl("/images/croptop.jpg");

Product p26 = new Product();
p26.setName("High Waist Jeans");
p26.setPrice(1899);
p26.setCategory("Women Fashion");
p26.setRating(4.7);
p26.setDiscount(15);
p26.setDescription("Comfortable high waist denim jeans.");
p26.setImageUrl("/images/jeans.jpg");

Product p27 = new Product();
p27.setName("Floral Summer Dress");
p27.setPrice(2499);
p27.setCategory("Women Fashion");
p27.setRating(4.9);
p27.setDiscount(18);
p27.setDescription("Elegant floral dress for summer.");
p27.setImageUrl("https://images.unsplash.com/photo-1496747611176-843222e1e57c");

Product p28 = new Product();
p28.setName("Oversized Shirt");
p28.setPrice(1299);
p28.setCategory("Women Fashion");
p28.setRating(4.6);
p28.setDiscount(12);
p28.setDescription("Trendy oversized casual shirt.");
p28.setImageUrl("https://images.unsplash.com/photo-1529139574466-a303027c1d8b");

Product p29 = new Product();
p29.setName("Women's Blazer");
p29.setPrice(2999);
p29.setCategory("Women Fashion");
p29.setRating(4.8);
p29.setDiscount(20);
p29.setDescription("Formal blazer for office and events.");
p29.setImageUrl("https://images.unsplash.com/photo-1483985988355-763728e1935b");

Product p30 = new Product();
p30.setName("Leather Handbag");
p30.setPrice(3499);
p30.setCategory("Women Fashion");
p30.setRating(4.9);
p30.setDiscount(10);
p30.setDescription("Premium leather handbag.");
p30.setImageUrl("https://images.unsplash.com/photo-1584917865442-de89df76afd3");

Product p31 = new Product();
p31.setName("Women's Heels");
p31.setPrice(2299);
p31.setCategory("Women Fashion");
p31.setRating(4.7);
p31.setDiscount(15);
p31.setDescription("Elegant heels for parties and events.");
p31.setImageUrl("/images/heels.jpg");

Product p32 = new Product();
p32.setName("Knitted Sweater");
p32.setPrice(1599);
p32.setCategory("Women Fashion");
p32.setRating(4.8);
p32.setDiscount(22);
p32.setDescription("Warm knitted sweater for winter.");
p32.setImageUrl("https://images.unsplash.com/photo-1434389677669-e08b4cac3105");

Product p33 = new Product();
p33.setName("Women's Track Suit");
p33.setPrice(2799);
p33.setCategory("Women Fashion");
p33.setRating(4.7);
p33.setDiscount(18);
p33.setDescription("Stylish jacket for everyday fashion.");
p33.setImageUrl("https://images.unsplash.com/photo-1515886657613-9f3515b0c78f");

Product p34 = new Product();
p34.setName("Women's Kurti");
p34.setPrice(1199);
p34.setCategory("Women Fashion");
p34.setRating(3.7);
p34.setDiscount(20);
p34.setDescription("Stylish kurti for everyday fashion.");
p34.setImageUrl("/images/kurti.jpg");

Product p35 = new Product();
p35.setName("Women's Dress");
p35.setPrice(2299);
p35.setCategory("Women Fashion");
p35.setRating(5.5);
p35.setDiscount(20);
p35.setDescription("Stylish bodycorn dress for party fashion.");
p35.setImageUrl("/images/dress.jpg");

Product p36 = new Product();
p36.setName("Women's Formal Dress");
p36.setPrice(3299);
p36.setCategory("Women Fashion");
p36.setRating(9.7);
p36.setDiscount(20);
p36.setDescription("Stylish forma suit for office fashion.");
p36.setImageUrl("/images/formal.jpg");

Product p37 = new Product();
p37.setName("Women's Classic");
p37.setPrice(1499);
p37.setCategory("Women Fashion");
p37.setRating(7.7);
p37.setDiscount(20);
p37.setDescription("Stylish classic outfit for everyday fashion.");
p37.setImageUrl("/images/classic.jpg");

Product p38 = new Product();
p38.setName("Women's Denim Suit");
p38.setPrice(2499);
p38.setCategory("Women Fashion");
p38.setRating(6.9);
p38.setDiscount(20);
p38.setDescription("Stylish classic denim outfit for everyday fashion.");
p38.setImageUrl("/images/denim.jpg");
  
Product p39 = new Product();
p39.setName("Women's Flowy Shirt Style One Piece");
p39.setPrice(3499);
p39.setCategory("Women Fashion");
p39.setRating(5.9);
p39.setDiscount(20);
p39.setDescription("Stylish Flowy Shirt Style One Piece for everyday fashion.");
p39.setImageUrl("/images/onepice.jpg");

Product p40 = new Product();
p40.setName("Leather Laptop Bag");
p40.setPrice(8999);
p40.setCategory("Women Fashion");
p40.setRating(7.9);
p40.setDiscount(21);
p40.setDescription("laptop backpack office bags for women with laptop compartment");
p40.setImageUrl("/images/bag.jpg");
        

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
        productRepository.save(p4);
        productRepository.save(p5);
        productRepository.save(p6);
        productRepository.save(p7);
        productRepository.save(p8);
        productRepository.save(p9);
        productRepository.save(p10);
        productRepository.save(p11);
        productRepository.save(p12);
        productRepository.save(p13);
        productRepository.save(p14);
        productRepository.save(p15);
        productRepository.save(p16);
        productRepository.save(p17);
        productRepository.save(p18);
productRepository.save(p19);
productRepository.save(p20);
productRepository.save(p21);
productRepository.save(p22);
productRepository.save(p23);
productRepository.save(p24);
productRepository.save(p25);
productRepository.save(p26);
productRepository.save(p27);
productRepository.save(p28);
productRepository.save(p29);
productRepository.save(p30);
productRepository.save(p31);
productRepository.save(p32);
productRepository.save(p33);
productRepository.save(p34);
productRepository.save(p35);
productRepository.save(p36);
productRepository.save(p37);
productRepository.save(p38);
productRepository.save(p39);
productRepository.save(p40);           

    

    return "redirect:/products";
}


}
