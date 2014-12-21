package moorka.ui.components

import moorka.rx._
import moorka.ui.element._
import moorka.ui.event._

import scala.language.implicitConversions

/**
 * Definition of HTML tags, attributes and properties
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object html {

  //---------------------------------------------------------------------------
  //
  //  Implicit conversions
  //
  //---------------------------------------------------------------------------

  implicit def _bound(node: State[ElementBase]): BoundComponentContainer = {
    new BoundComponentContainer(node)
  }

  implicit def _sequence(seq: Seq[ElementBase]): ElementSequence = {
    new ElementSequence(seq)
  }

  implicit def _text(text: String): ElementPropertyExtension[String] = {
    `textContent` := text
  }

  implicit def _reactiveText(text: State[String]): ElementBoundPropertyExtension[String] = {
    `textContent` := text
  }

  //---------------------------------------------------------------------------
  //
  //  HTML tags
  //
  //---------------------------------------------------------------------------

  /**
   * Heading level 1
   *
   *  MDN
   */
  def h1(xs: ElementEntry*) = new Element("h1", xs)
  /**
   * Heading level 2
   *
   *  MDN
   */
  def h2(xs: ElementEntry*) = new Element("h2", xs)
  /**
   * Heading level 3
   *
   *  MDN
   */
  def h3(xs: ElementEntry*) = new Element("h3", xs)
  /**
   * Heading level 4
   *
   *  MDN
   */
  def h4(xs: ElementEntry*) = new Element("h4", xs)
  /**
   * Heading level 5
   *
   *  MDN
   */
  def h5(xs: ElementEntry*) = new Element("h5", xs)
  /**
   * Heading level 6
   *
   *  MDN
   */
  def h6(xs: ElementEntry*) = new Element("h6", xs)
  /**
   * Defines the header of a page or section. It often contains a logo, the
   * title of the Web site, and a navigational table of content.
   *
   *  MDN
   */
  def header(xs: ElementEntry*) = new Element("header", xs)
  /**
   * Defines the footer for a page or section. It often contains a copyright
   * notice, some links to legal information, or addresses to give feedback.
   *
   *  MDN
   */
  def footer(xs: ElementEntry*) = new Element("footer", xs)


  // Grouping content
  /**
   * Defines a portion that should be displayed as a paragraph.
   *
   *  MDN
   */
  def p(xs: ElementEntry*) = new Element("p", xs)
  /**
   * Represents a thematic break between paragraphs of a section or article or
   * any longer content.
   *
   *  MDN
   */
  def hr(xs: ElementEntry*) = new Element("hr", xs)
  /**
   * Indicates that its content is preformatted and that this format must be
   * preserved.
   *
   *  MDN
   */
  def pre(xs: ElementEntry*) = new Element("pre", xs)
  /**
   * Represents a content that is quoted from another source.
   *
   *  MDN
   */
  def blockquote(xs: ElementEntry*) = new Element("blockquote", xs)
  /**
   * Defines an ordered list of items.
   *
   *  MDN
   */
  def ol(xs: ElementEntry*) = new Element("ol", xs)
  /**
   * Defines an unordered list of items.
   *
   *  MDN
   */
  def ul(xs: ElementEntry*) = new Element("ul", xs)
  /**
   * Defines an item of an list.
   *
   *  MDN
   */
  def li(xs: ElementEntry*) = new Element("li", xs)
  /**
   * Defines a definition list; a list of terms and their associated definitions.
   *
   *  MDN
   */
  def dl(xs: ElementEntry*) = new Element("dl", xs)
  /**
   * Represents a term defined by the next dd
   *
   *  MDN
   */
  def dt(xs: ElementEntry*) = new Element("dt", xs)
  /**
   * Represents the definition of the terms immediately listed before it.
   *
   *  MDN
   */
  def dd(xs: ElementEntry*) = new Element("dd", xs)
  /**
   * Represents a figure illustrated as part of the document.
   *
   *  MDN
   */
  def figure(xs: ElementEntry*) = new Element("figure", xs)
  /**
   * Represents the legend of a figure.
   *
   *  MDN
   */
  def figcaption(xs: ElementEntry*) = new Element("figcaption", xs)
  /**
   * Represents a generic container with no special meaning.
   *
   *  MDN
   */
  def div(xs: ElementEntry*) = new Element("div", xs)

  // Text-level semantics
  /**
   * Represents a hyperlink, linking to another resource.
   *
   *  MDN
   */
  def a(xs: ElementEntry*) = new Element("a", xs)
  /**
   * Represents emphasized text.
   *
   *  MDN
   */
  def em(xs: ElementEntry*) = new Element("em", xs)
  /**
   * Represents especially important text.
   *
   *  MDN
   */
  def strong(xs: ElementEntry*) = new Element("strong", xs)
  /**
   * Represents a side comment; text like a disclaimer or copyright, which is not
   * essential to the comprehension of the document.
   *
   *  MDN
   */
  def small(xs: ElementEntry*) = new Element("small", xs)
  /**
   * Strikethrough element, used for that is no longer accurate or relevant.
   *
   *  MDN
   */
  def s(xs: ElementEntry*) = new Element("s", xs)
  /**
   * Represents the title of a work being cited.
   *
   *  MDN
   */
  def cite(xs: ElementEntry*) = new Element("cite", xs)

  /**
   * Represents computer code.
   *
   *  MDN
   */
  def code(xs: ElementEntry*) = new Element("code", xs)

  /**
   * Subscript tag
   *
   *  MDN
   */
  def sub(xs: ElementEntry*) = new Element("sub", xs)
  /**
   * Superscript tag.
   *
   *  MDN
   */
  def sup(xs: ElementEntry*) = new Element("sup", xs)
  /**
   * Italicized text.
   *
   *  MDN
   */
  def i(xs: ElementEntry*) = new Element("i", xs)
  /**
   * Bold text.
   *
   *  MDN
   */
  def b(xs: ElementEntry*) = new Element("b", xs)
  /**
   * Underlined text.
   *
   *  MDN
   */
  def u(xs: ElementEntry*) = new Element("u", xs)

  /**
   * Represents text with no specific meaning. This has to be used when no other
   * text-semantic element conveys an adequate meaning, which, in this case, is
   * often brought by global attributes like class, lang, or dir.
   *
   *  MDN
   */
  def span(xs: ElementEntry*) = new Element("span", xs)
  /**
   * Represents a line break.
   *
   *  MDN
   */
  def br(xs: ElementEntry*) = new Element("br", xs)
  /**
   * Represents a line break opportunity, that is a suggested point for wrapping
   * text in order to improve readability of text split on several lines.
   *
   *  MDN
   */
  def wbr(xs: ElementEntry*) = new Element("wbr", xs)

  // Edits
  /**
   * Defines an addition to the document.
   *
   *  MDN
   */
  def ins(xs: ElementEntry*) = new Element("ins", xs)
  /**
   * Defines a removal from the document.
   *
   *  MDN
   */
  def del(xs: ElementEntry*) = new Element("del", xs)

  // Embedded content
  /**
   * Represents an image.
   *
   *  MDN
   */
  def img(xs: ElementEntry*) = new Element("img", xs)
  /**
   * Represents a nested browsing context, that is an embedded HTML document.
   *
   *  MDN
   */
  def iframe(xs: ElementEntry*) = new Element("iframe", xs)
  /**
   * Represents a integration point for an external, often non-HTML, application
   * or interactive content.
   *
   *  MDN
   */
  def embed(xs: ElementEntry*) = new Element("embed", xs)
  /**
   * Represents an external resource, which is treated as an image, an HTML
   * sub-document, or an external resource to be processed by a plug-in.
   *
   *  MDN
   */
  def `object`(xs: ElementEntry*) = new Element("object", xs)
  /**
   * Defines parameters for use by plug-ins invoked by object elements.
   *
   *  MDN
   */
  def param(xs: ElementEntry*) = new Element("param", xs)
  /**
   * Represents a video, and its associated audio files and captions, with the
   * necessary interface to play it.
   *
   *  MDN
   */
  def video(xs: ElementEntry*) = new Element("video", xs)
  /**
   * Represents a sound or an audio stream.
   *
   *  MDN
   */
  def audio(xs: ElementEntry*) = new Element("audio", xs)
  /**
   * Allows the authors to specify alternate media resources for media elements
   * like video or audio
   *
   *  MDN
   */
  def source(xs: ElementEntry*) = new Element("source", xs)
  /**
   * Allows authors to specify timed text track for media elements like video or
   * audio
   *
   *  MDN
   */
  def track(xs: ElementEntry*) = new Element("track", xs)
  /**
   * Represents a bitmap area that scripts can use to render graphics like graphs,
   * games or any visual images on the fly.
   *
   *  MDN
   */
  def canvas(xs: ElementEntry*) = new Element("canvas", xs)
  /**
   * In conjunction with area, defines an image map.
   *
   *  MDN
   */
  def map(xs: ElementEntry*) = new Element("map", xs)
  /**
   * In conjunction with map, defines an image map
   *
   *  MDN
   */
  def area(xs: ElementEntry*) = new Element("area", xs)


  // Tabular data
  /**
   * Represents data with more than one dimension.
   *
   *  MDN
   */
  def table(xs: ElementEntry*) = new Element("table", xs)
  /**
   * The title of a table.
   *
   *  MDN
   */
  def caption(xs: ElementEntry*) = new Element("caption", xs)
  /**
   * A set of columns.
   *
   *  MDN
   */
  def colgroup(xs: ElementEntry*) = new Element("colgroup", xs)
  /**
   * A single column.
   *
   *  MDN
   */
  def col(xs: ElementEntry*) = new Element("col", xs)
  /**
   * The table body.
   *
   *  MDN
   */
  def tbody(xs: ElementEntry*) = new Element("tbody", xs)
  /**
   * The table headers.
   *
   *  MDN
   */
  def thead(xs: ElementEntry*) = new Element("thead", xs)
  /**
   * The table footer.
   *
   *  MDN
   */
  def tfoot(xs: ElementEntry*) = new Element("tfoot", xs)
  /**
   * A single row in a table.
   *
   *  MDN
   */
  def tr(xs: ElementEntry*) = new Element("tr", xs)
  /**
   * A single cell in a table.
   *
   *  MDN
   */
  def td(xs: ElementEntry*) = new Element("td", xs)
  /**
   * A header cell in a table.
   *
   *  MDN
   */
  def th(xs: ElementEntry*) = new Element("th", xs)

  // Forms
  /**
   * Represents a form, consisting of controls, that can be submitted to a
   * server for processing.
   *
   *  MDN
   */
  def form(xs: ElementEntry*) = new Element("form", xs)
  /**
   * A set of fields.
   *
   *  MDN
   */
  def fieldset(xs: ElementEntry*) = new Element("fieldset", xs)
  /**
   * The caption for a fieldset.
   *
   *  MDN
   */
  def legend(xs: ElementEntry*) = new Element("legend", xs)
  /**
   * The caption of a single field
   *
   *  MDN
   */
  def label(xs: ElementEntry*) = new Element("label", xs)
  /**
   * A typed data field allowing the user to input data.
   *
   *  MDN
   */
  def input(xs: ElementEntry*) = new Element("input", xs)
  /**
   * A button
   *
   *  MDN
   */
  def button(xs: ElementEntry*) = new Element("button", xs)
  /**
   * A control that allows the user to select one of a set of options.
   *
   *  MDN
   */
  def select(xs: ElementEntry*) = new Element("select", xs)
  /**
   * A set of predefined options for other controls.
   *
   *  MDN
   */
  def datalist(xs: ElementEntry*) = new Element("datalist", xs)
  /**
   * A set of options, logically grouped.
   *
   *  MDN
   */
  def optgroup(xs: ElementEntry*) = new Element("optgroup", xs)
  /**
   * An option in a select element.
   *
   *  MDN
   */
  def option(xs: ElementEntry*) = new Element("option", xs)
  /**
   * A multiline text edit control.
   *
   *  MDN
   */
  def textarea(xs: ElementEntry*) = new Element("textarea", xs)
  
  /**
   * Defines the title of the document, shown in a browser's title bar or on the
   * page's tab. It can only contain text and any contained tags are not
   * interpreted.
   *
   * MDN
   */
  def title(xs: ElementEntry*) = new Element("title", xs)

  /**
   * Used to write inline CSS.
   *
   *  MDN
   */
  def style(xs: ElementEntry*) = new Element("style", xs)

  // Sections
  /**
   * Represents a generic section of a document, i.e., a thematic grouping of
   * content, typically with a heading.
   *
   *  MDN
   */
  def section(xs: ElementEntry*) = new Element("section", xs)
  /**
   * Represents a section of a page that links to other pages or to parts within
   * the page: a section with navigation links.
   *
   *  MDN
   */
  def nav(xs: ElementEntry*) = new Element("nav", xs)
  /**
   * Defines self-contained content that could exist independently of the rest
   * of the content.
   *
   *  MDN
   */
  def article(xs: ElementEntry*) = new Element("article", xs)
  /**
   * Defines some content loosely related to the page content. If it is removed,
   * the remaining content still makes sense.
   *
   *  MDN
   */
  def aside(xs: ElementEntry*) = new Element("aside", xs)
  /**
   * Defines a section containing contact information.
   *
   *  MDN
   */
  def address(xs: ElementEntry*) = new Element("address", xs)

  /**
   * Defines the main or important content in the document. There is only one
   * main element in the document.
   *
   *  MDN
   */
  def main(xs: ElementEntry*) = new Element("main", xs)

  // Text level semantics

  /**
   * An inline quotation.
   *
   *  MDN
   */
  def q(xs: ElementEntry*) = new Element("q", xs)
  /**
   * Represents a term whose definition is contained in its nearest ancestor
   * content.
   *
   *  MDN
   */
  def dfn(xs: ElementEntry*) = new Element("dfn", xs)
  /**
   * An abbreviation or acronym; the expansion of the abbreviation can be
   * represented in the title attribute.
   *
   *  MDN
   */
  def abbr(xs: ElementEntry*) = new Element("abbr", xs)
  /**
   * Associates to its content a machine-readable equivalent.
   *
   *  MDN
   */
  def data(xs: ElementEntry*) = new Element("data", xs)
  /**
   * Represents a date and time value; the machine-readable equivalent can be
   * represented in the datetime attribetu
   *
   *  MDN
   */
  def time(xs: ElementEntry*) = new Element("time", xs)
  /**
   * Represents a variable.
   *
   *  MDN
   */
  def `var`(xs: ElementEntry*) = new Element("`var`", xs)
  /**
   * Represents the output of a program or a computer.
   *
   *  MDN
   */
  def samp(xs: ElementEntry*) = new Element("samp", xs)
  /**
   * Represents user input, often from a keyboard, but not necessarily.
   *
   *  MDN
   */
  def kbd(xs: ElementEntry*) = new Element("kbd", xs)

  /**
   * Defines a mathematical formula.
   *
   *  MDN
   */
  def math(xs: ElementEntry*) = new Element("math", xs)
  /**
   * Represents text highlighted for reference purposes, that is for its
   * relevance in another context.
   *
   *  MDN
   */
  def mark(xs: ElementEntry*) = new Element("mark", xs)
  /**
   * Represents content to be marked with ruby annotations, short runs of text
   * presented alongside the text. This is often used in conjunction with East
   * Asian language where the annotations act as a guide for pronunciation, like
   * the Japanese furigana .
   *
   *  MDN
   */
  def ruby(xs: ElementEntry*) = new Element("ruby", xs)
  /**
   * Represents the text of a ruby annotation.
   *
   *  MDN
   */
  def rt(xs: ElementEntry*) = new Element("rt", xs)
  /**
   * Represents parenthesis around a ruby annotation, used to display the
   * annotation in an alternate way by browsers not supporting the standard
   * display for annotations.
   *
   *  MDN
   */
  def rp(xs: ElementEntry*) = new Element("rp", xs)
  /**
   * Represents text that must be isolated from its surrounding for bidirectional
   * text formatting. It allows embedding a span of text with a different, or
   * unknown, directionality.
   *
   *  MDN
   */
  def bdi(xs: ElementEntry*) = new Element("bdi", xs)
  /**
   * Represents the directionality of its children, in order to explicitly
   * override the Unicode bidirectional algorithm.
   *
   *  MDN
   */
  def bdo(xs: ElementEntry*) = new Element("bdo", xs)

  // Forms

  /**
   * A key-pair generator control.
   *
   *  MDN
   */
  def keygen(xs: ElementEntry*) = new Element("keygen", xs)
  /**
   * The result of a calculation
   *
   *  MDN
   */
  def output(xs: ElementEntry*) = new Element("output", xs)
  /**
   * A progress completion bar
   *
   *  MDN
   */
  def progress(xs: ElementEntry*) = new Element("progress", xs)
  /**
   * A scalar measurement within a known range.
   *
   *  MDN
   */
  def meter(xs: ElementEntry*) = new Element("meter", xs)


  // Interactive elements
  /**
   * A widget from which the user can obtain additional information
   * or controls.
   *
   *  MDN
   */
  def details(xs: ElementEntry*) = new Element("details", xs)
  /**
   * A summary, caption, or legend for a given details.
   *
   *  MDN
   */
  def summary(xs: ElementEntry*) = new Element("summary", xs)
  /**
   * A command that the user can invoke.
   *
   *  MDN
   */
  def command(xs: ElementEntry*) = new Element("command", xs)
  /**
   * A list of commands
   *
   *  MDN
   */
  def menu(xs: ElementEntry*) = new Element("menu", xs)
  
  //---------------------------------------------------------------------------
  //
  //  HTML attributes
  //
  //---------------------------------------------------------------------------

  /**
   * This is the single required attribute for anchors defining a hypertext
   * source link. It indicates the link target, either a URL or a URL fragment.
   * A URL fragment is a name preceded by a hash mark (#), which specifies an
   * internal target location (an ID) within the current document. URLs are not
   * restricted to Web (HTTP)-based documents. URLs might use any protocol
   * supported by the browser. For example, file, ftp, and mailto work in most
   * user agents.
   *
   * MDN
   */
  val href = ElementAttributeName("href")
  /**
   * The URI of a program that processes the information submitted via the form.
   * This value can be overridden by a formaction attribute on a button or
   * input element.
   *
   * MDN
   */
  val action = ElementAttributeName("action")
  /**
   * The HTTP method that the browser uses to submit the form. Possible values are:
   *
   * - post: Corresponds to the HTTP POST method ; form data are included in the
   *   body of the form and sent to the server.
   *
   * - get: Corresponds to the HTTP GET method; form data are appended to the
   *   action attribute URI with a '?' as a separator, and the resulting URI is
   *   sent to the server. Use this method when the form has no side-effects and
   *   contains only ASCII characters.
   *
   * This value can be overridden by a formmethod attribute on a button or
   * input element.
   *
   * MDN
   */
  val method = ElementAttributeName("method")
  /**
   * A name or keyword indicating where to display the response that is received
   * after submitting the form. In HTML 4, this is the name of, or a keyword
   * for, a frame. In HTML5, it is a name of, or keyword for, a browsing context
   * (for example, tab, window, or inline frame). The following keywords have
   * special meanings:
   *
   * - _self: Load the response into the same HTML 4 frame (or HTML5 browsing
   *   context) as the current one. This value is the default if the attribute
   *   is not specified.
   * - _blank: Load the response into a new unnamed HTML 4 window or HTML5
   *   browsing context.
   * - _parent: Load the response into the HTML 4 frameset parent of the current
   *   frame or HTML5 parent browsing context of the current one. If there is no
   *   parent, this option behaves the same way as _self.
   * - _top: HTML 4: Load the response into the full, original window, canceling
   *   all other frames. HTML5: Load the response into the top-level browsing
   *   context (that is, the browsing context that is an ancestor of the current
   *   one, and has no parent). If there is no parent, this option behaves the
   *   same way as _self.
   * - iframename: The response is displayed in a named iframe.
   */
  val target = ElementAttributeName("target")
  /**
   * On form elements (input etc.):
   *   Name of the element. For example used by the server to identify the fields
   *   in form submits.
   *
   * On the meta tag: 
   *   This attribute defines the name of a document-level metadata.
   *   This document-level metadata name is associated with a value, contained by
   *   the content attribute.
   *
   * MDN
   */
  val name = ElementAttributeName("name")
  /**
   * This attribute defines the alternative text describing the image. Users
   * will see this displayed if the image URL is wrong, the image is not in one
   * of the supported formats, or until the image is downloaded.
   *
   * MDN
   */
  val alt = ElementAttributeName("alt")
  /**
   * The blur event is raised when an element loses focus.
   *
   * MDN
   */
  val onselect = ElementAttributeName("onselect")
  /**
   * Specifies the function to be called when the window is scrolled.
   *
   * MDN
   */
  val onscroll = ElementAttributeName("onscroll")
  /**
   * The submit event is raised when the user clicks a submit button in a form
   * (<input type="submit"/>).
   *
   * MDN
   */
  val onsubmit = ElementAttributeName("onsubmit")
  /**
   * The reset event is fired when a form is reset.
   *
   * MDN
   */
  val onreset = ElementAttributeName("onreset")
  /**
   * This attribute names a relationship of the linked document to the current
   * document. The attribute must be a space-separated list of the link types
   * values. The most common use of this attribute is to specify a link to an
   * external style sheet: the rel attribute is set to stylesheet, and the href
   * attribute is set to the URL of an external style sheet to format the page.
   *
   *
   * MDN
   */
  val rel = ElementAttributeName("rel")
  /**
   * If the value of the type attribute is image, this attribute specifies a URI
   * for the location of an image to display on the graphical submit button;
   * otherwise it is ignored.
   *
   * MDN
   */
  val src = ElementAttributeName("src")
  /**
   * This attribute contains CSS styling declarations to be applied to the
   * element. Note that it is recommended for styles to be defined in a separate
   * file or files. This attribute and the style element have mainly the
   * purpose of allowing for quick styling, for example for testing purposes.
   *
   * MDN
   */
  val style = ElementAttributeName("style")
  /**
   * This attribute contains a text representing advisory information related to
   * the element it belongs too. Such information can typically, but not
   * necessarily, be presented to the user as a tooltip.
   *
   * MDN
   */
  val title = ElementAttributeName("title")
  /**
   * This attribute is used to define the type of the content linked to. The
   * value of the attribute should be a MIME type such as text/html, text/css,
   * and so on. The common use of this attribute is to define the type of style
   * sheet linked and the most common current value is text/css, which indicates
   * a Cascading Style Sheet format. You can use tpe as an alias for this
   * attribute so you don't have to backtick-escape this attribute.
   *
   * MDN
   */
  val `type` = ElementAttributeName("type")
  /**
   * Shorthand for the `type` attribute
   */
  val tpe = `type`
  /**
   *
   */
  val xmlns = ElementAttributeName("xmlns")
  /**
   * This attribute is a space-separated list of the classes of the element.
   * Classes allows CSS and Javascript to select and access specific elements
   * via the class selectors or functions like the DOM method
   * document.getElementsByClassName. You can use cls as an alias for this
   * attribute so you don't have to backtick-escape this attribute.
   *
   * MDN
   */
  val `class` = ElementAttributeName("class")
  /**
   * Shorthand for the `class` attribute
   */
  val cls = `class`
  /**
   * This attribute participates in defining the language of the element, the
   * language that non-editable elements are written in or the language that
   * editable elements should be written in. The tag contains one single entry
   * value in the format defines in the Tags for Identifying Languages (BCP47)
   * IETF document. If the tag content is the empty string the language is set
   * to unknown; if the tag content is not valid, regarding to BCP47, it is set
   * to invalid.
   *
   * MDN
   */
  val lang = ElementAttributeName("lang")
  /**
   * A hint to the user of what can be entered in the control. The placeholder
   * text must not contain carriage returns or line-feeds. This attribute
   * applies when the value of the type attribute is text, search, tel, url or
   * email; otherwise it is ignored.
   *
   * MDN
   */
  val placeholder = ElementAttributeName("placeholder")
  /**
   * This enumerated attribute defines whether the element may be checked for
   * spelling errors.
   *
   * MDN
   */
  val spellcheck = ElementAttributeName("spellcheck") := "spellcheck"
  /**
   * If the value of the type attribute is file, this attribute indicates the
   * types of files that the server accepts; otherwise it is ignored.
   *
   * MDN
   */
  val accept = ElementAttributeName("accept")
  /**
   * This attribute indicates whether the value of the control can be
   * automatically completed by the browser. This attribute is ignored if the
   * value of the type attribute is hidden, checkbox, radio, file, or a button
   * type (button, submit, reset, image).
   *
   * Possible values are "off" and "on"
   *
   * MDN
   */
  val autocomplete = ElementAttributeName("autocomplete")

  /**
   * Declares the character encoding of the page or script. Used on meta and
   * script elements.
   *
   * MDN
   */
  val charset = ElementAttributeName("charset")
  /**
   * This Boolean attribute indicates that the form control is not available for
   * interaction. In particular, the click event will not be dispatched on
   * disabled controls. Also, a disabled control's value isn't submitted with
   * the form.
   *
   * This attribute is ignored if the value of the type attribute is hidden.
   *
   * MDN
   */
  val disabled = ElementAttributeName("disabled") := "disabled"
  /**
   * Describes elements which belongs to this one. Used on labels and output
   * elements.
   *
   * MDN
   */
  val `for` = ElementAttributeName("for")
  /**
   * This Boolean attribute indicates that the user cannot modify the value of
   * the control. This attribute is ignored if the value of the type attribute
   * is hidden, range, color, checkbox, radio, file, or a button type.
   *
   * MDN
   */
  val readonly = ElementAttributeName("readonly") := "readonly"
  /**
   * This attribute specifies that the user must fill in a value before
   * submitting a form. It cannot be used when the type attribute is hidden,
   * image, or a button type (submit, reset, or button). The :optional and
   * :required CSS pseudo-classes will be applied to the field as appropriate.
   *
   * MDN
   */
  val required = ElementAttributeName("required") := "required"
  /**
   * The number of visible text lines for the control.
   *
   * MDN
   */
  val rows = ElementAttributeName("rows")
  /**
   * The visible width of the text control, in average character widths. If it
   * is specified, it must be a positive integer. If it is not specified, the
   * default value is 20 (HTML5).
   *
   * MDN
   */
  val cols = ElementAttributeName("cols")
  /**
   * The initial size of the control. This value is in pixels unless the value
   * of the type attribute is text or password, in which case, it is an integer
   * number of characters. Starting in HTML5, this attribute applies only when
   * the type attribute is set to text, search, tel, url, email, or password;
   * otherwise it is ignored. In addition, the size must be greater than zero.
   * If you don't specify a size, a default value of 20 is used.
   *
   * MDN
   */
  val size = ElementAttributeName("size")
  /**
   * This integer attribute indicates if the element can take input focus (is
   * focusable), if it should participate to sequential keyboard navigation, and
   * if so, at what position. It can takes several values:
   *
   * - a negative value means that the element should be focusable, but should
   *   not be reachable via sequential keyboard navigation;
   * - 0 means that the element should be focusable and reachable via sequential
   *   keyboard navigation, but its relative order is defined by the platform
   *   convention;
   * - a positive value which means should be focusable and reachable via
   *   sequential keyboard navigation; its relative order is defined by the value
   *   of the attribute: the sequential follow the increasing number of the
   *   tabindex. If several elements share the same tabindex, their relative order
   *   follows their relative position in the document).
   *
   * An element with a 0 value, an invalid value, or no tabindex value should be placed after elements with a positive tabindex in the sequential keyboard navigation order.
   */
  val tabindex = ElementAttributeName("tabindex")
  /**
   * The attribute describes the role(s) the current element plays in the 
   * context of the document. This can be used, for example, 
   * by applications and assistive technologies to determine the purpose of 
   * an element. This could allow a user to make informed decisions on which 
   * actions may be taken on an element and activate the selected action in a
   * device independent way. It could also be used as a mechanism for 
   * annotating portions of a document in a domain specific way (e.g., 
   * a legal term taxonomy). Although the role attribute may be used to add 
   * semantics to an element, authors should use elements with inherent 
   * semantics, such as p, rather than layering semantics on semantically 
   * neutral elements, such as div role="paragraph".
   *
   * http://www.w3.org/TR/role-attribute/#s_role_module_attributes
   */
  val role = ElementAttributeName("role")
  /**
   * This attribute gives the value associated with the http-equiv or name
   * attribute, depending of the context.
   *
   * MDN
   */
  val content = ElementAttributeName("content")
  /**
   * This enumerated attribute defines the pragma that can alter servers and
   * user-agents behavior. The value of the pragma is defined using the content
   * attribute and can be one of the following:
   *
   *   - content-language 
   *   - content-type 
   *   - default-style
   *   - refresh
   *   - set-cookie
   *
   * MDN
   */
  val httpEquiv = ElementAttributeName("http-equiv")
  /**
   * This attribute specifies the media which the linked resource applies to.
   * Its value must be a media query. This attribute is mainly useful when
   * linking to external stylesheets by allowing the user agent to pick
   * the best adapted one for the device it runs on.
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/link#attr-media
   */
  val media = ElementAttributeName("media")

  //---------------------------------------------------------------------------
  //
  //  HTML properties
  //
  //---------------------------------------------------------------------------

  val `textContent` = ElementPropertyName[String]("textContent")

  /**
   * The initial value of the control. This attribute is optional except when
   * the value of the type attribute is radio or checkbox.
   *
   * MDN
   */
  val `value` = ElementPropertyName[String]("value")

  /**
   * When the value of the type attribute is radio or checkbox, the presence of
   * this Boolean attribute indicates that the control is selected by default;
   * otherwise it is ignored.
   *
   * MDN
   */
  val `checked` = ElementPropertyName[Boolean]("checked")

  /**
   * This Boolean attribute lets you specify that a form control should have
   * input focus when the page loads, unless the user overrides it, for example
   * by typing in a different control. Only one form element in a document can
   * have the autofocus attribute, which is a Boolean. It cannot be applied if
   * the type attribute is set to hidden (that is, you cannot automatically set
   * focus to a hidden control).
   *
   * MDN
   */
  val `autofocus` = ElementPropertyName[Boolean]("autofocus")

  val `className` = ElementPropertyName[String]("className")

  //---------------------------------------------------------------------------
  //
  //  HTML events
  //
  //---------------------------------------------------------------------------

  object on {
    val `change` = ElementEventName(InputEventProcessor)

    val `input` = ElementEventName(ChangeEventProcessor)

    val `double-click` = ElementEventName(DoubleClickEventProcessor)

    val `click` = ElementEventName(ClickEventProcessor)

    val `touchend` = ElementEventName(TouchendEventProcessor)

    val `submit` = ElementEventName(SubmitEventProcessor)
  }

  //---------------------------------------------------------------------------
  //
  //  Custom extensions
  //
  //---------------------------------------------------------------------------

  // todo fomkin: needs big refactoring
  // we need something like
  // "classes += "class-from-css" when state"
  // "classes -= "class-from-css" when state"
  def useClassName(clsName: String, not: Boolean = false)
                  (implicit reaper: Reaper = Reaper.nice) = {
    new BoundExtensionFactory[Boolean](
          x => UseClassExtension(clsName, if (not) !x else x),
          x => UseClassBoundExtension(
            clsName,
            if (not) x.map(!_)
            else x
          )
        )
  }

  val `show` = useClassName("hidden", not = true)

  val `hide` = useClassName("hidden", not = false)
}
