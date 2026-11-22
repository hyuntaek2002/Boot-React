package org.zerock.apiserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.zerock.apiserver.domain.Cart;
import org.zerock.apiserver.domain.CartItem;
import org.zerock.apiserver.domain.Member;
import org.zerock.apiserver.domain.Product;
import org.zerock.apiserver.dto.CartItemDTO;
import org.zerock.apiserver.dto.CartItemListDTO;
import org.zerock.apiserver.repository.CartItemRepository;
import org.zerock.apiserver.repository.CartRepository;
import org.zerock.apiserver.repository.MemberRepository;
import org.zerock.apiserver.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Log4j2
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final MemberRepository memberRepository;

    private final ProductRepository productRepository;

    @Override
    public List<CartItemListDTO> addOrModify(CartItemDTO cartItemDTO) {

        String email = cartItemDTO.getEmail();
        Long pno = cartItemDTO.getPno();
        int qty = cartItemDTO.getQty();
        Long cino = cartItemDTO.getCino();

        if (cino != null) {

            Optional<CartItem> cartItemResult = cartItemRepository.findById(cino);
            CartItem cartItem = cartItemResult.orElseThrow();

            cartItem.changeQty(qty);
            cartItemRepository.save(cartItem);

            return getCartItems(email);
        }

        Cart cart = getCart(email);

        CartItem cartItem = cartItemRepository.getItemOfPno(email, pno);

        if (cartItem == null) {
            Product product = productRepository.getReferenceById(pno);
            cartItem = CartItem.builder().product(product).cart(cart).qty(qty).build();
        } else {
            cartItem.changeQty(qty);
        }

        cartItemRepository.save(cartItem);

        return getCartItems(email);
    }

    private Cart getCart(String email) {

        Cart cart = null;

        Optional<Cart> result = cartRepository.getCartOfMember(email);

        if (result.isEmpty()) {

            log.info("Cart of the member is not exist!!");

            Member member = memberRepository.getReferenceById(email);
            Cart tempCart = Cart.builder().owner(member).build();
            cart = cartRepository.save(tempCart);

        } else {
            cart = result.get();
        }
        return cart;
    }

    @Override
    public List<CartItemListDTO> getCartItems(String email) {
        return cartItemRepository.getItemsOfCartDTOByEmail(email);
    }

    @Override
    public List<CartItemListDTO> remove(Long cino) {

        Long cno = cartItemRepository.getCartFromItem(cino);

        log.info("cart no: " + cno);

        cartItemRepository.deleteById(cino);

        return cartItemRepository.getItemsOfCartDTOByCart(cno);
    }
}
