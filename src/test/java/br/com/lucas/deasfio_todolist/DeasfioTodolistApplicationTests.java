package br.com.lucas.deasfio_todolist;

import br.com.lucas.deasfio_todolist.entity.Todo;
import br.com.lucas.deasfio_todolist.repository.TodoRepository;
import br.com.lucas.deasfio_todolist.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class DeasfioTodolistApplicationTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    TodoService todoService;
    @BeforeEach
    void resetDatabase() {
        todoRepository.deleteAll();
        todoRepository.flush();
        // Força o commit das mudanças
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
        todoService = new TodoService(todoRepository);
    }

	@Test
    @DisplayName("Deve criar um Todo")
    void testCreateTodoSuccess() {
        var todo = new Todo("todo1", "desc todo 1", 1, false);
        processarMetodoHtpp(webTestClient
                .post(), todo);

    }

    @Test
    @DisplayName("Deve atualizar um Todo")
    void testUpdateTodoSuccess() {
        var todo = new Todo("todo1", "desc todo 1", 1, false);
        processarMetodoHtpp(webTestClient
                .post(), todo);

        Todo atualizado = retornarTodoDoBanco();

        processarMetodoHtpp(webTestClient
                .put(), atualizado);

    }

    @Test
    @DisplayName("Deve deve deletar um Todo")
    void testDeleteTodoSuccess() {
        var todo = new Todo("todo1", "desc todo 1", 1, false);
        processarMetodoHtpp(webTestClient
                .post(), todo);

        Todo atualizado = retornarTodoDoBanco();

        webTestClient
                .delete()
                .uri("/todos/{id}", atualizado.getId())
                .exchange()
                .expectStatus()
                .isOk();
    }

    private Todo retornarTodoDoBanco() {
        Todo atualizado = todoRepository.findAll().get(0);
        atualizado.setNome("Atualizado");
        atualizado.setDescricao("Descrição atualizada");
        atualizado.setPrioridade(5);
        atualizado.setRealizado(true);
        return atualizado;
    }

    private void processarMetodoHtpp(WebTestClient.RequestBodyUriSpec webTestClient, Todo todo) {
        webTestClient
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
    @DisplayName("Deve retornar erro quando Todos estiver com nome e descricao vazio")
	void testCreateTodoFailure() {
        webTestClient
                .post()
                .uri("/todos")
                .bodyValue(new Todo("", "", 1, false))
                .exchange()
                .expectStatus().isBadRequest();
	}

    @Test
    @DisplayName("Deve retornar todos os Todos salvos")
    void testFunidAll(){
        Todo todo1 = new Todo("Todo 1", "Descrição 1", 1, false);
        Todo todo2 = new Todo("Todo 2", "Descrição 2", 2,true);
        Todo todo3 = new Todo("Todo 3", "Descrição 3", 1, false);

        todoRepository.save(todo1);
        todoRepository.save(todo2);
        todoRepository.save(todo3);

        List<Todo> todoList = todoRepository.findAll();

        assertEquals(3, todoList.size());
    }

    @Test
    @DisplayName("Deve ordenar por prioridade decrescente e nome crescente")
    void testFindAllWithSort(){

        todoService = new TodoService(todoRepository);
        Todo todo1 = new Todo("A Todo", "Descrição A", 2, false);
        Todo todo2 = new Todo("B Todo", "Descrição B", 3,true);
        Todo todo3 = new Todo("C Todo", "Descrição C", 1, false);

        todoRepository.save(todo1);
        todoRepository.save(todo2);
        todoRepository.save(todo3);

        List<Todo> todoList = todoService.list();

        assertEquals(3, todoList.size());
        assertEquals("B Todo", todoList.get(0).getNome());
        assertEquals("A Todo", todoList.get(1).getNome());
        assertEquals("C Todo", todoList.get(2).getNome());
    }

    @Test
    @DisplayName("Deve deletar um Todo")
    void testDeleteTodo(){
        Todo todo1 = new Todo("A Todo", "Descrição A", 2, false);
        todoService.create(todo1);

        todoService.delete(todo1.getId());
        assertFalse(todoRepository.existsById(todo1.getId()));
        assertEquals(0, todoRepository.count());
    }

    @Test
    @DisplayName("Deve gerar erro quando tentar deletar com valor nulo")
    void testDeleteTodoNulo(){
        try{
            todoService.delete(null);
            fail("não deveriia gravar.");
        } catch (Exception e){
            assertEquals("ID não pode ser nulo", e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve atualizar um Todo")
    void testeUpdateTodo(){
        Todo todo = new Todo("Original", "Descrição original", 1, false);
        Todo todoDoBanco = todoRepository.save(todo);

        todoDoBanco.setNome("Atualizado");
        todoDoBanco.setDescricao("Descrição atualizada");
        todoDoBanco.setPrioridade(5);
        todoDoBanco.setRealizado(true);

        try {
            todoService.update(todoDoBanco);
        }catch (Exception e){
            fail(e.getMessage());
        }

        Todo updatedTodo = todoRepository.getReferenceById(todoDoBanco.getId());
        assertEquals(todoDoBanco.getId(), updatedTodo.getId());
        assertEquals("Atualizado", updatedTodo.getNome());
        assertEquals("Descrição atualizada", updatedTodo.getDescricao());
        assertEquals(5, updatedTodo.getPrioridade());
        assertTrue(updatedTodo.isRealizado());
    }
}
