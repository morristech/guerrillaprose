package de.handler.mobile.android.bachelorapp.app.api;

import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.RestClientErrorHandling;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.util.List;

import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.database.MediaType;

/**
 * Interface defining the API to the server
 */
@Rest(
        rootUrl = "http://mortoncornelius.no-ip.biz/guerrilla-prose/public/index.php/",
        converters = {GsonHttpMessageConverter.class})
public interface GuerrillaService extends RestClientErrorHandling {

    /**
     * user methods
     */
    @Post("users/login?email={email}&password={password}")
    String login(String email, String password);

    @Post("users/logout?email={email}")
    Boolean logout(String email);

    @Get("users")
    List<Guerrilla> getUsers();

    @Get("users/{email}")
    Guerrilla getUser(String email);

    @Post("users?email={email}&password={password}&surname={surname}&name={name}")
    Guerrilla setUser(String email, String password, String surname, String name);

    @Put("users/{id}?email={email}&surname={surname}&name={name}")
    @RequiresAuthentication
    Guerrilla updateUser(Long id, String email, String surname, String name);

    @Delete("users/{id}")
    @RequiresAuthentication
    Long deleteUser(Long id);



    /**
     * media methods
     */
    @Get("medias")
    List<Media> getMedias();

    @Get("medias/{id}")
    Media getMedia(Long id);

    @Get("medias/base64/{id}")
    String getMediaBase64String(Long id);

    @Post("medias?type={mediaTypeId}&mediaAuthor={mediaAuthor}")
    Long setMedia(Long mediaTypeId, String base64, String mediaAuthor);

    @Put("medias/{id}?type={mediaTypeId}")
    @RequiresAuthentication
    Long updateMedia(Long id, String base64, Long mediaTypeId);

    @Delete("medias/{id}")
    @RequiresAuthentication
    Long deleteMedia(Long id);



    /**
     * media type methods
     */
    @Get("mediaTypes")
    List<MediaType> getMediaTypes();

    @Get("mediaTypes/{mediaType}")
    MediaType getMediaType(String mediaType);

    @Post("mediaTypes?mediaType={mediaType}")
    Long setMediaType(String mediaType);

    @Delete("mediaTypes/{mediaType}")
    @RequiresAuthentication
    Long deleteMediaType(String mediaType);



    /**
     * prose methods
     */
    @Get("proses")
    List<GuerrillaProse> getProses();

    @Get("proses/tag/{tag}")
    List<GuerrillaProse> getProses(String tag);

    @Get("proses/user/{id}")
    List<GuerrillaProse> getProseForUser(Long id);

    @Get("proses/{remoteId}")
    GuerrillaProse getProse(Long remoteId);

    @Post("proses?tag={tag}&title={title}&text={text}&mediaId={mediaId}&remoteMediaId={remoteMediaId}&userId={userId}&author={author}")
    GuerrillaProse setProse(String tag, String title, String text, Long mediaId, Long remoteMediaId, Long userId, String author);

    @Put("proses/{remoteId}?tag={tag}&title={title}&text={text}&mediaId={mediaId}&remoteMediaId={remoteMediaId}&userId={userId}&author={author}")
    @RequiresAuthentication
    GuerrillaProse updateProse(Long remoteId, String tag, String title, String text, Long mediaId, Long remoteMediaId, Long userId, String author);

    @Delete("proses/{remoteId}")
    @RequiresAuthentication
    Long deleteProse(Long remoteId);



    /**
     * get most popular tags from server
     */
    @Get("tags/popular")
    List<String> getPopularTags();


    /**
     * Test server connectivity
     * Just send a ping to server and wait for answer
     */
    @Get("")
    Boolean pingServer();


    /**
     * add http basic authentication to the request
     * The credentials are set for put and delete operations in the controllers
     */
    void setHttpBasicAuth(String username, String password);
}
