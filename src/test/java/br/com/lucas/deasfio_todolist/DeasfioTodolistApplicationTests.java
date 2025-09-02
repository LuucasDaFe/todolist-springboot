package br.com.lucas.deasfio_todolist;

import br.com.lucas.deasfio_todolist.entity.Todo;
import br.com.lucas.deasfio_todolist.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class DeasfioTodolistApplicationTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private TodoRepository todoRepository;
    @BeforeEach
    void resetDatabase() {
        todoRepository.deleteAll();
        todoRepository.flush();
        // Força o commit das mudanças
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }
	@Test
	void testCreateTodoSuccess() {
        var todo = new Todo("todo1", "desc todo 1", 1, false);
        webTestClient
                .post()
                .uri("/todos")
                .bodyValue(todo)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].nome").isEqualTo(todo.getNome())
                .jsonPath("$[0].descricao").isEqualTo(todo.getDescricao())
                .jsonPath("$[0].realizado").isEqualTo(todo.isRealizado())
                .jsonPath("$[0].prioridade").isEqualTo(todo.getPrioridade());

    }

    @Test
	void testCreateTodoFailure() {
        webTestClient
                .post()
                .uri("/todos")
                .bodyValue(new Todo("", "", 1, false))
                .exchange()
                .expectStatus().isBadRequest();
	}

}
