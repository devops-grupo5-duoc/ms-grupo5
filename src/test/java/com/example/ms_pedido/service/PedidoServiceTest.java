package com.example.ms_pedido.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.ms_pedido.models.Pedido;
import com.example.ms_pedido.repository.PedidoRepository;

class PedidoServiceTest {

    private PedidoRepository repository;
    private PedidoService service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoRepository.class);
        service = new PedidoService();
        ReflectionTestUtils.setField(service, "pedidoRepository", repository);
    }

    @Test
    void findAllDevuelvePedidosDelRepositorio() {
        Pedido pedido = new Pedido();
        when(repository.findAll()).thenReturn(List.of(pedido));

        assertEquals(List.of(pedido), service.findAll());
        verify(repository).findAll();
    }

    @Test
    void findByIdDevuelvePedidoExistente() {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(pedido));

        assertSame(pedido, service.findById(5L));
    }

    @Test
    void findByIdInexistenteDevuelveNull() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.findById(99L));
    }

    @Test
    void saveGuardaPedidoEnRepositorio() {
        Pedido pedido = new Pedido();
        when(repository.save(pedido)).thenReturn(pedido);

        assertSame(pedido, service.save(pedido));
        verify(repository).save(pedido);
    }

    @Test
    void patchActualizaSoloCamposInformados() {
        Pedido existente = new Pedido();
        existente.setIdPedido(7L);
        existente.setNombreInvitado("Nombre original");
        existente.setTotal(new BigDecimal("12000"));
        Pedido cambios = new Pedido();
        cambios.setTotal(new BigDecimal("15000"));
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Pedido resultado = service.patchPedido(7L, cambios);

        assertEquals(new BigDecimal("15000"), resultado.getTotal());
        assertEquals("Nombre original", resultado.getNombreInvitado());
        verify(repository).save(existente);
    }

    @Test
    void patchInexistenteNoGuardaCambios() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.patchPedido(99L, new Pedido()));
        verify(repository, never()).save(any(Pedido.class));
    }

    @Test
    void deleteByIdExistenteEliminaPedido() {
        when(repository.existsById(3L)).thenReturn(true);

        service.deleteById(3L);

        verify(repository).deleteById(3L);
    }

    @Test
    void deleteByIdInexistenteLanzaExcepcion() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.deleteById(99L));
        verify(repository, never()).deleteById(anyLong());
    }
}
